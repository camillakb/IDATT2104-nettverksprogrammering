const http = require("http");
const fs = require("fs").promises;
const host = "localhost";
const port = "8000"; 

let index_file;
const requestListener = (req, res) => {
  console.info("Received a request!");
  console.info(req.method);
  console.info(req.url);

  if (req.method === "GET") {
    switch (req.url) {
      case "/":
        res.setHeader("Content-Type", "text/html");
        res.writeHead(200);
        res.end(index_file);
        break;
      default:
        error(res, 404, "Resource not found");
    }

  } else if (req.method === "POST") {
    res.setHeader("Content-Type", "application/json");

    switch (req.url) {
      case "/api":
        let body = "";
        req.on("data", (chunk) => (body += chunk.toString()));
        req.on("end", () => {
          const code = JSON.parse(body).code;

          if (code !== undefined) {
            writeToFile(code);
            const { exec } = require("child_process");
            
            exec('docker build "compiler" -t compiler', (cmd, buildLog, buildError) => {
              if (cmd !== null) {
                res.setHeader('Access-Control-Allow-Origin', '*');
                res.setHeader('Access-Control-Allow-Methods', 'OPTIONS, GET, POST');
                res.setHeader('Access-Control-Allow-Headers', 'content-type');
                res.writeHead(400).end(
                  JSON.stringify({ result: `Compilation failed` })
                );
                return;
              }

              exec("docker run compiler", (cmd, runOutput, runError) => {
                
                if (!runError.trim() === "") {
                  error(res, 400, runError);
                
                } else {
                  res.setHeader('Access-Control-Allow-Origin', '*');
                  res.setHeader('Access-Control-Allow-Methods', 'OPTIONS, GET, POST');
                  res.setHeader('Access-Control-Allow-Headers', 'content-type');
                  res.writeHead(200).end(
                    JSON.stringify({ result: `${buildLog}\n\n${runOutput}` })
                  );
                }
              });

            });

          } else {
            error(res, 400, "Could not find the code input");
          }
        });

        break;
        
      default:
        error(res, 404, "Resource not found");
    }
  } else if (req.method === "OPTIONS") {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'OPTIONS, GET, POST');
    res.setHeader('Access-Control-Allow-Headers', 'content-type');
    res.setHeader('Access-Control-Max-Age', 2592000); //30 days
    res.statusCode = 204;
    res.end();
  }
};

const error = (res, code, text) => {
  res.setHeader("Content-Type", "application/json");
  res.writeHead(code);
  res.end(JSON.stringify({ error: text }));
};

const writeToFile = (code) => {
  fs.writeFile(`${__dirname}/compiler/main.cpp`, code, (err) => {
    if (err) throw err;
  });
};

const server = http.createServer(requestListener);
fs.readFile(`${__dirname}/index.html`)
  .then((contents) => {
    index_file = contents;
    server.listen(port, host, () => {
      console.log(`Server is running on http://${host}:${port}`);
    });
  })
  .catch((err) => {
    console.error(`Could not read index.html file: ${err}`);
    process.exit(1);
  });