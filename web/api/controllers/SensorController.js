/**
 * SensorController
 *
 * @description :: Server-side logic for managing sensors
 * @help        :: See http://sailsjs.org/#!/documentation/concepts/Controllers
 */

module.exports = {

    change: function(req, res) {
        if (!req.isSocket) {
            return res.badRequest();
        }
        sails.sockets.join(req, 'funSockets');
        sails.sockets.broadcast('funSockets', 'hello', req);
        return res.json({status: 'done.'});
    }
};

