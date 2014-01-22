
// Project directories.
base_dir = System.getProperty('user.dir')
cache_dir = "${base_dir}/.cache"
content_dir = "${base_dir}/content"
theme_dir = "${base_dir}/theme"
source_dir = [content_dir, theme_dir, "${cache_dir}/compass"]
include_dir = "${theme_dir}/includes"
layout_dir = "${theme_dir}/layouts"
destination_dir = "${base_dir}/target"

// Excluded files or directories.
excludes = ['/sass/.*', '/src/.*', '/target/.*']

// Embedded code configuration.
code_enabled_files = ['html', 'md', 'markdown', 'xml', 'css', 'rst', 'adoc', 'asciidoc']
code_allowed_files = ['txt', 'js', 'rb']