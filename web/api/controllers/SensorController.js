/**
 * SensorController
 *
 * @description :: Server-side logic for managing sensors
 * @help        :: See http://sailsjs.org/#!/documentation/concepts/Controllers
 */

module.exports = {

    change: function(req, res) {
//        sails.sockets.join(req, 'funSockets');
//        sails.sockets.broadcast('funSockets', 'hello', req);
        var io = sails.io;
        io.sockets.emit('change', req.query);

        return res.json({
            status: 'done.'
        });
    }
};

