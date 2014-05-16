(function(global, $) {
  var connectionSpec,
      client,
      welcome;
  
  connectionSpec = {port: 17500, serviceName: 'welcome'};
  client = $.fm.ws.makeClient(connectionSpec, {
    onError: function(error) {
      global.console.log('Connection Error: ' + error);            
    },
    onConnectFailed: function() {
      global.console.log('Connect failed!');            
    },
    onConnect: function() {
      global.console.log('Client connected.');
      welcome.sayHello({
        onSuccess: function(result) {},
        onFailure: function(error) {}
      });
    },
    onDisconnect: function() {
      global.console.log('Client disconnected.');            
    }
  });
  welcome = client.defNamespace('welcome');
  welcome.defRequest('sayHello');
  
  $(function() { client.open(); }); 
})(this, (this.jQuery || this));

