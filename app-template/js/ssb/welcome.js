(function(global, $) {
  var connectionSpec,
      client,
      welcome,
      showResponse;
  
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
        onSuccess: function(result) { showResponse(result); },
        onFailure: function(error) {}
      });
    },
    onDisconnect: function() {
      global.console.log('Client disconnected.');            
    }
  });
  welcome = client.defNamespace('welcome');
  welcome.defRequest('sayHello');
  showResponse = function(response) {
    var messageView = $('.ssb-welcome-view')
                        .first()
                        .find('.ssb-welcome-message')
                        .first();
   
    if (messageView) {
      if (response === true) {
        response = 'Well, apparently nothing...'
      } else {
        response = (response || '').toString();
      }
                          
      messageView.text(response);
    }                      
  };
  
  $(function() { client.open(); }); 
})(this, (this.jQuery || this));

