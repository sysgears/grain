/*-
script: true
-*/
<%
  def toCanonicalPath = { String baseDir, String relativePath ->
    new File(baseDir, relativePath).canonicalPath.replace("\\", "/")
  }
  
  def cssPath = toCanonicalPath site.cache_dir, '/compass/stylesheets'
  new File(cssPath).mkdirs()
%>
require 'susy'

# Require any additional compass plugins here.
project_type = :stand_alone

# Publishing paths
http_path = "/"
http_images_path = "/images"
http_generated_images_path = "/images"
http_fonts_path = "/fonts"
css_path = "${cssPath}"

# Local development paths
sass_path = "${toCanonicalPath site.theme_dir, '/sass'}"
generated_images_path = "${toCanonicalPath site.cache_dir, '/compass/images'}"
images_path = "${toCanonicalPath site.theme_dir, '/images'}"
fonts_path = "${toCanonicalPath site.theme_dir, '/fonts'}"

line_comments = false
output_style = :compressed
