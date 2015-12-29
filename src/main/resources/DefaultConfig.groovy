Locale.setDefault(Locale.US)

// Jetty port
jetty_port = 4000

// Project directories.
base_dir = System.getProperty('user.dir')
destination_dir = "${base_dir}/target"
cache_dir = "${base_dir}/.cache"
content_dir = "${base_dir}/content"
theme_dir = "${base_dir}/theme"
source_dir = [content_dir, theme_dir, "${cache_dir}/compass"]
include_dir = ["${theme_dir}/includes"]
layout_dir = ["${theme_dir}/layouts"]


// Excluded files or directories.
excludes = ['/sass/.*', '/src/.*', '/target/.*', '.*.swp']

// Binary files or directories that contain binary files.
binary_files = [/(?i).*\.(png|jpg|jpeg|gif|ico|bmp|swf|avi|mkv|ogg|mp3|mp4|eot|otf|ttf|woff)$/]

// Non-script files or directories with non-script files.
non_script_files = [/(?i).*\.(js|css)$/]

// Absolute links
generate_absolute_links = false

// Date time format
datetime_format = 'yyyy-MM-dd HH:mm'
