const net = require('net');
const crypto = require('crypto');

//simple HTTP server responds with a simple WebSocket client test
const httpServer = net.createServer((connection) => {
  connection.on('data', () => {
    let content = `<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
  </head>
  <body>
    WebSocket test page
    <script>
      let ws = new WebSocket('ws://localhost:3001');
      ws.onmessage = event => alert('Message from server: ' + event.data);
      ws.onopen = () => ws.send('hello');
    </script>
  </body>
</html>
`;
    connection.write('HTTP/1.1 200 OK\r\nContent-Length: ' + content.length + '\r\n\r\n' + content);
  });
});
httpServer.listen(3000, () => {
  console.log('HTTP server listening on port 3000');
});

//websocket server
const wsServer = net.createServer((connection) => {
  console.log('Client connected');

  connection.on('data', (data) => {

    if (data.toString().startsWith("GET / HTTP/1.1")) {
      //gets the websocket key, concat sequence and hash with sha1
      const key = data.toString().split("\r\n").filter((line) => { return line.startsWith("Sec-WebSocket-Key"); })[0].split(": ")[1];
      const hasher = crypto.createHash("sha1");
      const full_key = key.concat("258EAFA5-E914-47DA-95CA-C5AB0DC85B11");
      hasher.update(full_key);
      //decrypt and find base64 value
      const reply_key = hasher.digest().toString("base64");

      const handshake = `HTTP/1.1 101 Switching Protocols
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Accept: ${reply_key}

`;
      connection.write(handshake);
    }

    if (data.at(0) !== 0x81) {
      return;
    }

    if ((data.at(1) & 0x7F) > 125) {
      return;
    }

    const payload_length = data.at(1) & 0x7F;
    const mask = [data.at(2), data.at(3), data.at(4), data.at(5)];
    let message = "";

    for (let i = 6; i < 6 + payload_length; i++) {
      let byte = data[i] ^ mask[(i-6) % 4];
      message = message.concat(String.fromCharCode(byte));
    }

    console.log(message);

    let reply_message = [0x81, payload_length];

    for (var i = 0; i < payload_length; i++) {
      reply_message.push(message.charCodeAt(i));
    }

    connection.write(Buffer.from(reply_message));
  });

  connection.on('end', () => {
    console.log('Client disconnected');
  });

  
});
wsServer.on('error', (error) => {
  console.error('Error: ', error);
});

wsServer.listen(3001, () => {
  console.log('WebSocket server listening on port 3001');
});