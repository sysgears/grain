Locale.setDefault(Locale.US)

// Jetty port
jetty_port = 4000

// Project directories.
base_dir = System.getProperty('user.dir')
cache_dir = "${base_dir}/.cache"
content_dir = "${base_dir}/content"
theme_dir = "${base_dir}/theme"
source_dir = [content_dir, theme_dir, "${cache_dir}/compass", "${cache_dir}/less"]
include_dir = ["${theme_dir}/includes"]
layout_dir = ["${theme_dir}/layouts"]
destination_dir = "${base_dir}/target"

// Binary files
binary_files = [/(?i).*\.(png|jpg|jpeg|gif|ico|bmp|swf|avi|mkv|ogg|mp3|mp4|eot|otf|ttf|woff)$/]

// Absolute links
generate_absolute_links = false

// Date time format
datetime_format = 'yyyy-MM-dd HH:mm'

// Excluded files or directories.
excludes = ['/sass/.*', '/src/.*', '/target/.*']
