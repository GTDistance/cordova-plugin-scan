var exec = require('cordova/exec');
var scan = {
	recognize:function(callback) {
		exec(callback, callback, "scan", "recognize", []);
	}
};
module.exports = scan;