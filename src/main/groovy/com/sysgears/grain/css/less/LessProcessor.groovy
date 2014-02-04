package com.sysgears.grain.css.less

import com.sysgears.grain.config.Config
import com.sysgears.grain.preview.ConfigChangeListener
import com.sysgears.grain.util.FileUtils
import groovy.util.logging.Slf4j
import org.lesscss.LessCompiler

import javax.inject.Inject

/**
 * LESS files compiler.
 */
@javax.inject.Singleton
@Slf4j
class LessProcessor implements ConfigChangeListener {
    
    /** Site config */
    @Inject private Config config
    
    /** Less engine */
    @Inject private LessCompiler engine

    /**
     * @inheritDoc 
     */
    @Override
    void configChanged() {
        compile()
    }

    /**
     * Compiles LESS files
     */
    public void compile() {
        def inputDirs = (config.less.input_dirs ?: []).collect { new File(it as String) }
        if (inputDirs.any { it.exists() } ) {
            def outputDir = new File(config.less.output_dir as String)
            def outputFiles = []
            outputDir.exists() && outputDir.eachFileRecurse {
                outputFiles += it
            }
            def outputTimeStamp = outputFiles.max { it.lastModified() }
            def files = []
            inputDirs.each { inputDir ->
                inputDir.eachFileRecurse {
                    files += it
                }
            }
            def allInputFiles = files.findAll { it.getExtension() == 'less' }
            def inputTimeStamp = allInputFiles.max { it.lastModified() }
            
            def inputFiles = [:] as Map<File, List<File> >
            inputDirs.each { inputDir ->
                inputFiles[inputDir] = []
                inputDir.eachFileRecurse { file ->
                    if (file.getExtension() == 'less' &&
                            !config.less.excludes.any { file.canonicalPath.matches(it as String) }) {
                        inputFiles[inputDir].add(file)
                    }
                }
            }
            
            def recompile = outputTimeStamp < inputTimeStamp

            recompile = recompile || inputFiles.any { inputDir, inputFileList ->
                inputFileList.any { inputFile ->
                    !getOutputFile(inputFile, inputDir, outputDir).exists()
                }
            }
                
            if (recompile) {
                FileUtils.removeDir(outputDir)
                log.info "Compiling LESS files..."
                
                inputFiles.each { inputDir, inputFileList ->
                    inputFileList.each { inputFile ->
                        def outputFile = getOutputFile(inputFile, inputDir, outputDir)
                        FileUtils.createDirs(outputFile.parentFile)
                        println inputFile
                        println outputFile
                        engine.compile(inputFile, outputFile)
                    }
                }
            }
        }
    }

    /**
     * Returns css file that corresponds to given input less file
     * 
     * @param outputDir output dir
     * @param inputDir input dir
     * @param inputFile input less file
     * 
     * @return output css file
     */
    private static File getOutputFile(File inputFile, File inputDir, File outputDir) {
         new File(outputDir, inputFile.canonicalPath - inputDir.canonicalPath - '.less' + '.css')
    }
}
