/**
 * SensorController
 *
 * @description :: Server-side logic for managing sensors
 * @help        :: See http://sailsjs.org/#!/documentation/concepts/Controllers
 */

module.exports = {

    'change/:event': function(req, res) {
        console.log('[' + new Date() + ']', req.params, req.query);
        var io = sails.io;
        io.sockets.emit(req.params['event'], req.query);
        return res.json({
            status: 'done.'
        });
    },
};

