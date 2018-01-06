var pkgjson = require('./package.json');

var config = {
  pkg: pkgjson,
  app: 'app/src',
  dist: 'dist'
}

module.exports = function (grunt) {
  // Configuration
  grunt.initConfig({
    config: config,
    pkg: config.pkg,
    bower: grunt.file.readJSON('./.bowerrc'),
    uglify: {
      options: {
        banner: '/*! <%= pkg.name %> lib - v<%= pkg.version %> -' +
          '<%= grunt.template.today("yyyy-mm-dd") %> */'
      },
      dist: {
        files: {
          '<%= config.dist %>/js/event-engine-<%= pkg.version %>.min.js': [
            '<%= config.app %>/**/*.js',
            '<%= bower.directory %>/reconnectingWebsocket/reconnecting-websocket.js',
          ]
        }
      }
    }
  });

  grunt.loadNpmTasks('grunt-contrib-uglify');

  grunt.registerTask('default', [
    'uglify'
  ]);
};
