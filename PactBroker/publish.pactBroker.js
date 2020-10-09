const pact = require("@pact-foundation/pact-node");
const path = require("path");

let pactBrokerUsername = process.env.PACT_BROKER_USERNAME || "pact_workshop";
let pactBrokerPassword = process.env.PACT_BROKER_PASSWORD || "pact_workshop";

const gitHash =
  require("child_process")
    .execSync("git rev-parse --short HEAD")
    .toString()
    .trim() + Math.floor(Date.now() / 1000);

const opts = {
  pactFilesOrDirs: [path.resolve(__dirname, "../pacts/")],
  pactBroker: "http://localhost:8000",
  //pactBrokerToken: "jiH7q2QKCiMvYW4Y1OREpQ",
  pactBrokerUsername: pactBrokerUsername,
  pactBrokerPassword: pactBrokerPassword,
  tags: ["prod", "test"],
  consumerVersion: gitHash,
};

pact
  .publishPacts(opts)
  .then(() => {
    console.log("Pact contract publishing complete!");
    console.log("");
    console.log(`Head over to ${pactBrokerUrl} and login with`);
    console.log(`=> Username: ${pactBrokerUsername}`);
    console.log(`=> Password: ${pactBrokerPassword}`);
    console.log("to see your published contracts.");
  })
  .catch((e) => {
    console.log("Pact contract publishing failed: ", e);
  });
