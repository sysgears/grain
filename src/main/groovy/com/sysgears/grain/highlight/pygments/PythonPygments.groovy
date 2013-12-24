/*
 * Copyright (c) 2013 SysGears, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sysgears.grain.highlight.pygments

import com.sysgears.grain.init.GrainSettings
import com.sysgears.grain.log.StreamLogger
import com.sysgears.grain.log.StreamLoggerFactory
import groovy.util.logging.Slf4j
import net.sf.json.JSONObject

import javax.inject.Inject
import javax.inject.Named
import java.util.concurrent.CountDownLatch

/**
 * Pygments code highlighter integration as external Python process.
 */
@Named
@javax.inject.Singleton
@Slf4j
class PythonPygments extends Pygments {

    /** Random number generator */
    protected @Inject Random random

    /** Mentos standard output writer */
    protected BufferedOutputStream bos

    /** Mentos standard input reader */
    protected DataInputStream dis

    /** Whether use bundled pygments or system-supplied pygments */
    protected boolean bundledPygments

    /** Stream logger factory */
    @Inject private StreamLoggerFactory streamLoggerFactory

    /** Grain settings */
    @Inject private GrainSettings settings

    /** Process streams logger */
    private StreamLogger streamLogger

    /** Mutex for pygments starting and using */
    private CountDownLatch latch

    /**
     * Creates an instance of pygments code highlighter
     *
     * @param bundledPygments whether use bundled pygments or system-supplied pygments
     */
    public PythonPygments(boolean bundledPygments) {
        this.bundledPygments = bundledPygments
    }

    /**
     * Creates an instance of pygments code highlighter that uses bundled pygments
     */
    public PythonPygments() {
        this(true)
    }

    /**
     * Highlights code using Pygments 
     *
     * @param code a code to highlight
     * @param language the language of the code
     *
     * @return highlighted code HTML
     */
    public String highlight(String code, String language) {
        try {
            if (latch == null) {
                launchPygments()
            }
            latch.await()
            // Generate random ID for Mentos which is used to check that input and output are in sync
            def id = new String((1..8).collect { (65 + random.nextInt(25)) as char } as char[])
            // Encode source code
            def sourceBytes = "${id}  ${code.replace('\r', '')}  ${id}".bytes
            // Pass various options to highlight pygments method
            def kwargs = [id: id, bytes: sourceBytes.length, options: [outencoding: 'utf-8'], lexer: language]
            // Create header for Mentos
            def hdrBytes = JSONObject.fromObject([method: 'highlight', args: null, kwargs: kwargs]).toString().bytes
            // Format size of the header as binary string
            def sizeBytes = String.format("%32s", Integer.toBinaryString(hdrBytes.length)).replace(' ', '0').bytes

            // Write request to Mentos
            bos.write(sizeBytes)
            bos.write(hdrBytes)
            bos.write(sourceBytes)
            bos.flush()
            
            if (log.debugEnabled) {
                log.debug(new String(sizeBytes) + new String(hdrBytes) + new String(sourceBytes))
            }

            // Parse response from Mentos

            // Get header size
            def headerSizeData = new byte[33]
            dis.readFully(headerSizeData)
            def headerSize = Integer.parseInt(new String(headerSizeData).trim(), 2) + 1
            log.debug new String(headerSizeData) + " >> " + headerSize

            // Get header
            def headerData = new byte[headerSize]
            dis.readFully(headerData)
            log.debug new String(headerData)
            def header = JSONObject.fromObject(new String(headerData))

            if (header.error) {
                throw new RuntimeException("Error from mentos: [${header.error}]")
            }

            // Get highlighted code
            def codeData = new byte[header.bytes]
            dis.readFully(codeData)
            def str = new String(codeData).trim()
            log.debug str

            // Check that ID's match and hence input and output are in sync
            if (str[-8..-1] != id) {
                throw new RuntimeException("ID mismatch with Mentos, expected ID: ${id}, actual: ${str[-8..-1]}")
            }

            // Return highlighted code
            def highligting = str[10..str.length() - 11]

            highligting.trim()
        } catch (t) {
            log.error("Error highlighting code:\n ${code}", t)
            null
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void launchPygments() {
        latch = new CountDownLatch(1)
        thread = Thread.start {
            try {
                log.info 'Launching Python pygments...'
                def env = ["SIMPLEJSON_HOME=${new File(settings.toolsHome, 'simplejson').canonicalPath}"]
                if (bundledPygments) {
                    env += ["PYGMENTS_HOME=${new File(settings.toolsHome, 'pygments-main').canonicalPath}"]
                }
                def process = Runtime.runtime.exec([PythonFinder.pythonCmd, "mentos.py"] as String[],
                        env as String[],
                        new File(settings.toolsHome, 'mentos'))
                bos = new BufferedOutputStream(process.out)
                dis = new DataInputStream(process.in)
                latch.countDown()
                streamLogger = streamLoggerFactory.create(process.err)
                streamLogger.start()
                def watcher = Thread.start {
                    process.waitFor()
                    streamLogger.interrupt()
                }
                streamLogger.join()
                process.destroy()
                watcher.join()
                bos.close()
                dis.close()
                log.info 'Python pygments finished'
            } catch (t) {
                log.error("Error launching Pygments", t)
                latch.countDown()
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void start() {
    }

    /**
     * @inheritDoc
     */
    @Override
    public void stop() {
        if (latch) {
            latch.await()
            streamLogger?.interrupt()
            thread.join()
            latch = null
        }
    }
}
