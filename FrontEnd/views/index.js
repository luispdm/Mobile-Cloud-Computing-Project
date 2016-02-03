var express = require('express');
var router = express.Router();
var mongoose = require('mongoose');
var crypto = require('crypto');
var bodyParser = require('body-parser');
//var path = require('path');

var winston = require('winston');
winston.add(winston.transports.File, { filename: 'mylogfile.log' });

//----------------------------CONNESSIONE AL DB
mongoose.connect('mongodb://localhost/BackEnd');
var db = mongoose.connection;
db.on('error', console.error.bind(console, 'connection error:'));
db.once('open', function () {
    console.log("Connection established");
});

//----------------------------BODY PARSER
router.use(bodyParser.json());

//----------------------------EVENT SCHEMA
var eventSchema = mongoose.Schema({
    username: String,
    name: String,
    description: String,
    dateStartEvent: { type: Date, default: Date.now },
    dateEndEvent: Date,
    calendar: String,
    place: String,
    color: String,
    notification : Boolean,
    sharedWith: []
});
var Event = mongoose.model('Event', eventSchema);

//----------------------------USER SCHEMA
var userSchema = mongoose.Schema({
    username: String,
    password: String,
    token: String,
    lastAction: Date
});
var User = mongoose.model('User', userSchema);

//----------------------------CALENDAR SCHEMA
var calendarSchema = mongoose.Schema({
    name: String,
    description: String,
    owner: String,
    sharedWith: []
});
var Calendar = mongoose.model('Calendar', calendarSchema);

//----------------------------------------------- Functions --------------------------------------------------

//Gestione delle richieste
router.get('/', function(req, res) {
    res.sendFile('public/index.html');
});

//----------------------------LOGIN
//generazione del token come hash

router.post('/logincheck', function(req, res) {
    truncateFields(req);
    var passwordsha = crypto.createHash('sha256');
    passwordsha.update(req.body.password);
    var passwordUser = passwordsha.digest('hex');
    var username = req.body.username || "";
    User.findOne({ username: username.toString() , password : passwordUser.toString()}, function (err, user) {
        if (user){
            var sha = crypto.createHash('sha256');
            sha.update(Math.random().toString());
            var token = sha.digest('hex');
            User.update({'username': user.username}, {token : token, lastAction: Date.now()}, function (err, raw) {
                if (err) {
                    sendResponse('error', 500, "Impossible to complete the login", null, res);
                }else{
                    //res.sendFile('privateArea.html', { root: path.join(__dirname, '../public') });
                    User.findOne({ username: username.toString() , password : passwordUser.toString(), token : token.toString()}, function (err, user) {
                        var cleanUser = {
                            user: user.username,
                            token:user.token,
                            lastAction: user.lastAction
                        };
                        sendResponse('info', 200, "Login complete", cleanUser, res);
                    });
                }
            });
        }else{
            sendResponse('error', 500, "incorrect user or password", null, res);
        }
    });
});

//----------------------------REGISTRATION


router.post('/registration', function(req, res) {
    truncateFields(req);
    if(!req.body.username){
        return sendResponse('error', 500, "No username sent", null, res);
    }
    User.findOne({ username: req.body.username.toString() }, function (err, user) {
        if (!user){
            var sha = crypto.createHash('sha256');
            sha.update(Math.random().toString());

            var passwordsha = crypto.createHash('sha256');
            passwordsha.update(req.body.password);

            var newUser = new User({
                username: req.body.username,
                password: passwordsha.digest('hex'),
                token: sha.digest('hex'),
                lastAction: Date.now()
            });

            newUser.save(function (err, newUser) {
                if (err) return sendResponse('error', 500, "Internal server error", err, res);
                logEvent("New user created");
            });

            var cleanUser = {
                username: newUser.username,
                token: newUser.token,
                lastAction: newUser.lastAction
            };

            var newCalendar = new Calendar({
                name: "Default",
                description: "",
                owner: req.body.username
            });

            newCalendar.save(function (err, newCalendar) {
                if (err) return sendResponse('error', 500, "Internal server error", err, res);
                logEvent("New calendar created");
                sendResponse('info', 200, "User created", cleanUser, res);
               // res.sendFile('privateArea.html', { root: path.join(__dirname, '../public') });
            });

        }else{
            sendResponse('error', 500, "Username already in use", null, res);
        }
    });
});


//informazioni sul singolo calendario
router.get('/users/:username/token/:token/calendars/:id', function(req, res) {
    truncateFields(req);
    var id = req.params.id;
    var username = req.params.username;
    var token = req.params.token;

    if(!id.match(/^[0-9a-fA-F]{24}$/)) {
        return sendResponse('error', 404, "Calendar not found", {}, res);
    }

    checkToken(username, token, function(err, result){
        if(!result){return sendResponse('error', 500, "", err, res);}
        Calendar.find({"_id": id.toString() , $or: [ { owner: username.toString()}, { sharedWith: username.toString() } ]}, function (err, calendar) {
            if(calendar.length==0) {
                return sendResponse('error', 404, "Calendar not found", err, res);
            }
            if (err){
                return sendResponse('error', 500, "", err, res);
            }
            sendResponse('info', 200, "", calendar, res);
        });
    });
});


//lista di tutti i calendari di un utente
router.get('/users/:username/token/:token/calendars', function(req, res) {
    truncateFields(req);
    var username = req.params.username;
    var token = req.params.token;

    checkToken(username, token, function(err, result){
        if(!result){return sendResponse('error', 500, "", err, res);}
        Calendar.find({$or: [ { owner: username.toString()}, { sharedWith: username.toString() } ]}, function (err, calendars) {
            if(calendars.length==0) {
                return sendResponse('error', 404, "No calendars found", err, res);
            }
            if (err){
                sendResponse('error', 500, "", err, res);
            }
            sendResponse('info', 200, "", calendars, res);
        });
    });
});


//tutte le informazioni su un evento
router.get('/users/:username/token/:token/events/:id', function(req, res) {
    truncateFields(req);
    var id = req.params.id;
    var username = req.params.username;
    var token = req.params.token;
    var i,calendarId = [];

    if(!id.match(/^[0-9a-fA-F]{24}$/)) {
        return sendResponse('error', 404, "Event not found", {}, res);
    }

    checkToken(username, token, function(err, result){
        if(!result){return sendResponse('error', 500, "", err, res);}
        Calendar.find({$or: [ { owner: username.toString()}, { sharedWith: username.toString() } ]}, function (err, calendar) {
            if (err){return sendResponse('error', 500, "", err, res);}

            for(i=0;i<calendar.length;i++){
                calendarId[i] = calendar[i]._id.toString();
            }

            Event.find({"_id": id.toString(), $or: [ { username: username.toString()}, { sharedWith: username.toString() } , {calendar: {$in: calendarId }} ] }, function (err, events) {
                if(events.length==0) {
                    return sendResponse('error', 404, "Event not found", err, res);
                }
                if (err){
                    return sendResponse('error', 500, "", err, res);
                }
                sendResponse('info', 200, "", events, res);
            });
        });
    });
});

//lista di tutti gli eventi di un utente
router.get('/users/:username/token/:token/events', function(req, res) {
    truncateFields(req);
    var username = req.params.username;
    var token = req.params.token;
    var i;
    var calendarId = [];

    checkToken(username, token, function(err, result){
        if(!result){return sendResponse('error', 500, "", err, res);}
        Calendar.find({$or: [ { owner: username.toString()}, { sharedWith: username.toString() } ]}, function (err, calendar) {
            if (err){return sendResponse('error', 500, "", err, res);}

            for(i=0;i<calendar.length;i++){
                calendarId[i] = calendar[i]._id.toString();
            }

            Event.find({$or: [ { username: username.toString()}, { sharedWith: username.toString() } , {calendar: {$in: calendarId }} ] }, function (err, events) {
                if(events.length==0) {
                    return sendResponse('error', 404, "No events found", err, res);
                }
                if (err){
                    return sendResponse('error', 500, "", err, res);
                }
                sendResponse('info', 200, "", events, res);
            });
        });
    });
});

//lista con filtri pasati tramite JSON !!controllare il filtro per date!!
//funziona anche girando solo req.body, ma così mi perdo il controllo sui campi in ingresso e il range di date
router.post('/users/:username/token/:token/search', function(req, res) {
    truncateFields(req);
    var username = req.params.username;
    var token = req.params.token;
    var i;
    var calendarId = [];

    checkToken(username, token, function(err, result){
        if(!result){return sendResponse('error', 500, "", err, res);}

        var query = {};
        var calendar = req.body.calendar;
        var place = req.body.place;

        if(req.body.username){
            query.username = req.body.username;
        }
        if(req.body.name){
            query.name = req.body.name;
        }
        if(req.body.description){
            query.description = req.body.description;
        }
        if(place){
            query.place = place;
        }
        if(calendar){
            query.calendar = calendar;
        }

        if(req.body.dateStartEvent) {
            var dateObj = new Date(parseInt(req.body.dateStartEvent)); //MILLISECOND FROM START
            var month1 = dateObj.getUTCMonth(); //months from 0-11
            var day1 = dateObj.getUTCDate();
            var year1 = dateObj.getUTCFullYear();

            if (!req.body.dateEndEvent) {
                query.dateStartEvent = {"$gte": new Date(year1, month1, day1), "$lt": new Date(year1, month1, day1 + 1)};
                query.dateEndEvent = {"$gte": new Date(year1, month1, day1), "$lt": new Date(year1, month1, day1 + 1)};
            } else {
                var dateObj2 = new Date(parseInt(req.body.dateEndEvent)); //MILLISECOND FROM START
                var month2 = dateObj2.getUTCMonth(); //months from 0-11
                var day2 = dateObj2.getUTCDate();
                var year2 = dateObj2.getUTCFullYear();
                query.dateStartEvent = {"$gte": new Date(year1, month1, day1), "$lt": new Date(year2, month2, day2+1)};
                query.dateEndEvent = {"$gte": new Date(year1, month1, day1), "$lt": new Date(year2, month2, day2+1)};
            }
        }

        Calendar.find({$or: [ { owner: username.toString()}, { sharedWith: username.toString() } ]}, function (err, calendar) {
            if (err){return sendResponse('error', 500, "", err, res);}

            for(i=0;i<calendar.length;i++){
                calendarId[i] = calendar[i]._id.toString();
            }

            query.$or = [ { username: username.toString()}, { sharedWith: username.toString() } , {calendar: {$in: calendarId }} ];
            console.log(query);

            Event.find(query, function (err, events) {
                if (err){return sendResponse('error', 500, "", err, res);}
                sendResponse('info', 200, "", events, res);
            });
        });
    });
});

//rimozione calendario
//rimuove solo il nominativo dell'utente dalla lista di chi possiede il calendario, se gli eventi ed il calendario appartengono solo a quella persona li elimina
router.delete('/users/:username/token/:token/calendars/:calendar', function (req, res) {
    truncateFields(req);
    var username = req.params.username;
    var token = req.params.token;
    var id = req.params.calendar;

    if(!id.match(/^[0-9a-fA-F]{24}$/)) {
        return sendResponse('error', 404, "Calendar not found", {}, res);
    }

    checkToken(username, token, function(err, result){
        if(!result){return sendResponse('error', 500, "", err, res);}
        Calendar.find({"_id": id.toString() , $or: [ { owner: username.toString()}, { sharedWith: username.toString() } ]}, function (err, calendar) {
            if (err){return sendResponse('error', 500, "", err, res);}
            if(calendar.length>0) {
                Calendar.remove({'_id' : id}, function (err) {
                    if (err){return sendResponse('error', 500, "", err, res);}

                    Event.remove({'calendar' : id}, function(err) {
                        if (err){return sendResponse('error', 500, "", err, res);}
                        sendResponse('info', 200, "Calendar and related events removed succesfully", {}, res);
                    });
                });
            } else {
                sendResponse('info', 404, "This calendar doesn't exist", {}, res);
            }
        });
    });
});

//rimozione di un evento
router.delete('/users/:username/token/:token/events/:id', function (req, res) {
    truncateFields(req);
    var username = req.params.username;
    var token = req.params.token;
    var id = req.params.id;
    var i, calendarId = [];

    if(!id.match(/^[0-9a-fA-F]{24}$/)) {
        return sendResponse('error', 404, "Event not found", {}, res);
    }

    checkToken(username, token, function(err, result){
        if(!result){return sendResponse('error', 500, "", err, res);}
        Calendar.find({$or: [ { owner: username.toString()}, { sharedWith: username.toString() } ]}, function (err, calendar) {
            if (err){return sendResponse('error', 500, "", err, res);}

            for(i=0;i<calendar.length;i++){
                calendarId[i] = calendar[i]._id.toString();
            }
            //find forse superflua
            Event.find({"_id": id.toString(), $or: [ { username: username.toString()}, { sharedWith: username.toString() } , {calendar: {$in: calendarId }} ] }, function (err, events) {
                if (err){return sendResponse('error', 500, "", err, res);}
                if(events.length>0){
                    Event.remove({'_id' : id}, function(err) {
                        if (err){return sendResponse('error', 500, "", err, res);}
                        sendResponse('info', 200, "Event deleted", {}, res);
                    });
                }else{
                    sendResponse('info', 404, "Event doesn't exist", {}, res);
                }
            });
        });
    });
});

//creazione di un nuovo calendario
router.post('/users/:username/token/:token/calendars', function(req, res) {
    truncateFields(req);
    var username = req.params.username;
    var token = req.params.token;
    checkToken(username, token, function(err, result){
        if(!result){return sendResponse('error', 500, "", err, res);}
        var newCal = new Calendar({
            name: req.body.name || "Unnamed calendar",
            description: req.body.description || "",
            owner: username,
            sharedWith: req.body.sharedWith || []
        });

        newCal.save(function (err, newCal) {
            if (err){return sendResponse('error', 500, "", err, res);}
            sendResponse('info', 200, "New calendar created", newCal, res);
        });
    });
});


// inserimento di un nuovo evento, inserire valori di default
router.post('/users/:username/token/:token/events', function(req,res){
    truncateFields(req);
    var username = req.params.username;
    var token = req.params.token;
    checkToken(username, token, function(err, result){
        if(!result){return sendResponse('error', 500, "", err, res);}

        if(req.body.calendar) {
            if(!req.body.calendar.match(/^[0-9a-fA-F]{24}$/)) {
                return sendResponse('error', 404, "Calendar not valid", {}, res);
            }
            Calendar.find({"_id": req.body.calendar.toString(), $or: [ { owner: username.toString()}, { sharedWith: username.toString() } ]}, function (err, calendar) {
                if (err) {return sendResponse('error', 500, "", err, res);}
                if(calendar.length==0){
                    return sendResponse('error', 404, "Calendar not valid", {}, res);
                }
                var newEvent = new Event({
                    username: req.body.username || username,
                    name: req.body.name || "Unnamed event",
                    description: req.body.description || "",
                    dateStartEvent: req.body.dateStartEvent || Date.now(),
                    dateEndEvent: req.body.dateEndEvent || Date.now()+3600000,
                    calendar: req.body.calendar,
                    place: req.body.place || "",
                    notification : req.body.notification || false,
                    color: req.body.color || "#ffffff",
                    sharedWith: req.body.sharedWith || []
                });

                newEvent.save(function (err, newEvent) {
                    if (err){return sendResponse('error', 500, "", err, res);}
                    sendResponse('info', 200, "New event created", newEvent, res);
                });
            });
        }else{
            Calendar.findOne({ owner: username.toString() , name: "Default"}, function (err, calendar) {
                if (err) {return sendResponse('error', 500, "", err, res);}
                if(!calendar){
                    return sendResponse('error', 404, "No calendar exists. Impossible to create an event", {}, res);
                }
                var newEvent = new Event({
                    username: req.body.username || username,
                    name: req.body.name || "Unnamed event",
                    description: req.body.description || "",
                    dateStartEvent: req.body.dateStartEvent || Date.now(),
                    dateEndEvent: req.body.dateEndEvent || Date.now()+3600000,
                    calendar: calendar._id,
                    place: req.body.place || "",
                    notification : req.body.notification || false,
                    color: req.body.color || "#ffffff",
                    sharedWith: req.body.sharedWith || []
                });

                newEvent.save(function (err, newEvent) {
                    if (err){return sendResponse('error', 500, "", err, res);}
                    sendResponse('info', 200, "New event created", newEvent, res);
                });
            });
        }



    });
});


//aggiornamento campi calendario
router.put('/users/:username/token/:token/calendars/:id', function(req, res) {
    truncateFields(req);
    var id = req.params.id;
    var username = req.params.username;
    var token = req.params.token;

    if(!id.match(/^[0-9a-fA-F]{24}$/)) {
        return sendResponse('error', 404, "Calendar not found", {}, res);
    }

    checkToken(username, token, function(err, result){
        if(!result){return sendResponse('error', 500, "", err, res);}
        Calendar.findOneAndUpdate({"_id": id.toString() , $or: [ { owner: username.toString()}, { sharedWith: username.toString() } ]}, req.body, function (err, calendar) {
            if(!calendar) {
                return sendResponse('error', 404, "Calendar not found", err, res);
            }
            if (err) {
                return sendResponse('error', 500, "", err, res);
            }
            var updatedCaledar = {
                name: req.body.name || calendar.name,
                description: req.body.description || calendar.description,
                sharedWith: req.body.sharedWith || calendar.sharedWith
            };
            sendResponse('info', 200, "Calendar updated", updatedCaledar, res);
        });
    });
});

//update di un evento
router.put('/users/:username/token/:token/events/:id', function(req, res) {
    truncateFields(req);
    var id = req.params.id;
    var username = req.params.username;
    var token = req.params.token;
    var i,calendarId = [];

    if(!id.match(/^[0-9a-fA-F]{24}$/)) {
        return sendResponse('error', 404, "Event not found", {}, res);
    }

    checkToken(username, token, function(err, result){
        if(!result){return sendResponse('error', 500, "", err, res);}
        Calendar.find({$or: [ { owner: username.toString()}, { sharedWith: username.toString() } ]}, function (err, calendar) {
            if (err){return sendResponse('error', 500, "", err, res);}

            for(i=0;i<calendar.length;i++){
                calendarId[i] = calendar[i]._id.toString();
            }

            Event.findOneAndUpdate({"_id": id.toString(), $or: [ { username: username.toString()}, { sharedWith: username.toString() } , {calendar: {$in: calendarId }} ] }, req.body, function (err, events) {
                if(!events) {
                    return sendResponse('error', 404, "Event not found", err, res);
                }
                if (err){
                    return sendResponse('error', 500, "", err, res);
                }
                var updatedEvent = {
                    name: req.body.name || events.name,
                    description: req.body.description || events.description,
                    dateStartEvent: req.body.dateStartEvent || events.dateStartEvent,
                    dateEndEvent: req.body.dateEndEvent || events.dateEndEvent,
                    place: req.body.place || events.place,
                    notification : req.body.notification || events.notification,
                    color: req.body.color || events.color,
                    sharedWith: req.body.sharedWith || events.sharedWith
                };
                sendResponse('info', 200, "Event successfully updated", updatedEvent, res);
            });
        });
    });
});

//________________IMPORT-EXPORT CALENDARIO

//esportazione calendario + tutti gli eventi ad esso collegati
router.get('/users/:username/token/:token/calendars/:id/export', function(req, res) {
    truncateFields(req);
    var username = req.params.username;
    var token = req.params.token;
    var id = req.params.id;
    var exportedObject;

    if(!id.match(/^[0-9a-fA-F]{24}$/)) {
        return sendResponse('error', 404, "Calendar not found", {}, res);
    }

    checkToken(username, token, function(err, result){
        if(!result){return sendResponse('error', 500, "", err, res);}
        Calendar.find( {_id: id.toString(), $or: [ { owner: username.toString()}, { sharedWith: username.toString() } ]},  function (err, calendars) {
            if(calendars.length==0) {
                return sendResponse('error', 404, "Calendar not found. Export aborted", err, res);
            }
            if (err) {
                return sendResponse('error', 500, "", err, res);
            }

            calendars = calendars[0];

            Event.find({calendar: calendars._id.toString()}, function (err, events) {
                if (err){return sendResponse('error', 500, "", err, res);}

                exportedObject = {
                    "name": calendars.name,
                    "description": calendars.description,
                    "owner": calendars.owner,
                    "sharedWith": calendars.sharedWith,
                    "events": events
                };
                sendResponse('info', 200, "", exportedObject, res);
            });
        });
    });
});


//importazione calendario + eventi
router.post('/users/:username/token/:token/calendars/import', function(req, res) {
    truncateFields(req);
    var username = req.params.username;
    var token = req.params.token;
    var newEvents = [];
    var i;

    checkToken(username, token, function(err, result){
        if(!result){return sendResponse('error', 500, "", err, res);}
        var newCal = new Calendar({
            name: req.body.name,
            description: req.body.description,
            owner: username, //il nuovo owner è colui che importa il calendario
            sharedWith: []
        });

        newCal.save(function (err, newCal) {
            if (err){return sendResponse('error', 500, "", err, res);}

            for(i=0;i<req.body.events.length;i++) {
                newEvents = new Event({
                    username: username,
                    name: req.body.events[i].name,
                    description: req.body.events[i].description,
                    dateStartEvent: req.body.events[i].dateStartEvent,
                    dateEndEvent: req.body.events[i].dateEndEvent,
                    calendar: newCal._id, //gli eventi hanno l'id del calendario al quale appartengono, quindi nel campo calendar ci va l'id del calendario appena creato, giusto?
                    place: req.body.events[i].place,
                    notification: req.body.events[i].notification,
                    color: req.body.events[i].color,
                    sharedWith: []
                });
            }
            Event.create(newEvents, function (err, newEvent) {
                if (err) {
                    return sendResponse('error', 500, "Error during import events", err, res);
                }
                sendResponse('info', 200, "", null, res);
            });
        });
    });
});


//--------------------------------------FUNZIONI
function truncateFields(req) {
    if(req.body) {
        var i,k;
        for(i in req.body) {
            if(req.body[i].length > 100 && i!="description" && i!="sharedWith" && i!="events") {
                req.body[i] = req.body[i].substring(0, 100);
            }
            if(i=="description" && req.body[i].length > 200) {
                req.body[i] = req.body[i].substring(0, 200);
            }
            if(i=="sharedWith") {
                for(k=0;k<req.body[i].length;k++) {
                    if(req.body[i][k].length > 100) {
                        req.body[i][k] = req.body[i][k].substring(0, 100);
                    }
                }
            }
            if(i=="events") {
                truncateFields(req.body[i]);
            }
        }
    }
    if(req.params) {
        var j;
        for(j in req.params) {
            if(req.params[j].length > 100) {
                req.params[j] = req.params[j].substring(0, 100);
            }
        }
    }

}

function checkToken(username, token, callback) {
    User.findOne({username: username, token: token}, function(err, user){
        if (err){
            callback(err, false);
        }
        if(user!=null){
            if((parseInt(user.lastAction.getTime())+120000)>Date.now()){
                User.update({'username': user.username}, { lastAction: Date.now()}, function (err, raw) {
                    callback(err, true);
                });
            }else{
                User.update({'username': user.username}, { token: "" , lastAction: null}, function (err, raw) {
                    if (err) {
                        callback(err, false);
                    }else{
                        callback({err:"Session timeout"}, false);
                    }
                });
            }
        }else{
            callback({err:"User not found"}, false);
        }
    });
}

function sendResponse(type, status, message, json, res){
    if(!json){json = {};}
    if(message && status!=200){
        json.error = message;
    }else{
        //json.info = message;
    }
    winston.log(type, 'Status: '+status+' info: '+JSON.stringify(json));
    res.status(status).json(json);
}

function logEvent(message, json){
    if(!json){json = {};}
    winston.log('info', 'Message: '+message+' info: '+JSON.stringify(json));
}

//___________________________________UTILS
//probabilmente da eliminare nella versione definitiva

// funzione per la lista di tutti gli eventi
/*
router.get('/list', function(req, res) {
    Event.find({}, function (err, events) {
        if (err) return console.error(err);
        res.json(events);
    })
});
*/
module.exports = router;