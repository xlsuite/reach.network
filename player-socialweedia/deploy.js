var spawnSync = require("child_process").spawnSync,
fs = require("fs"),
credentialsPath = "private-keys/player-chromeapp/oauth-credentials.json",
utf8 = function() {return {encoding: "utf8"};},
app_id = (process.env.CIRCLE_BRANCH === "master" ? "production_app_id" : "test_app_id"),
publishVersion;

(function incrementPatchVersion() {
  var manifestFilePath = "app/manifest.json",
  d = new Date(),
  manifest = JSON.parse(fs.readFileSync("app/manifest.json", utf8())),
  dayMinutes = d.getHours() * 60 + d.getMinutes(),
  dayPct = dayMinutes / 1440,
  patchVer;

  patchVer = parseInt(((dayPct) + "").split(".")[1].substr(0,4)) + 1000;

  manifest.version = (d.getFullYear() - 2000) + "." +
  (d.getMonth() + 1) + "." +
  d.getDate() + "." +
  patchVer;

  publishVersion = manifest.version;

  fs.writeFileSync
  (manifestFilePath, JSON.stringify(manifest, null, 2), utf8());
}());

zip = spawnSync("zip", ["-r", "app", "app"], utf8());
console.log(zip.stdout);

credentials = JSON.parse(fs.readFileSync(credentialsPath, utf8()));

accessTokenRequest = spawnSync("curl", ["--data",
"refresh_token=" + credentials.refresh_token +
"&client_id=" + credentials.client_id +
"&client_secret=" + credentials.client_secret +
"&grant_type=refresh_token",
"https://www.googleapis.com/oauth2/v3/token"], utf8());

accessToken = JSON.parse(accessTokenRequest.stdout).access_token;

console.log("Uploading...");

chromeWebStoreUploadRequest = spawnSync("curl", [
"-H", "Authorization: Bearer " + accessToken, 
"-H", "x-goog-api-version: 2",
"-X", "PUT",
"-T", "app.zip",
"-vv",
"https://www.googleapis.com/upload/chromewebstore/v1.1/items/" + credentials[app_id]]);

console.log(JSON.parse(chromeWebStoreUploadRequest.stdout.toString()).uploadState);

if (chromeWebStoreUploadRequest.stdout.toString().indexOf("FAILURE") > -1) {
  console.log(chromeWebStoreUploadRequest.stdout.toString());
  process.exit(1);
}

console.log("Publishing version " + publishVersion);
chromeWebStorePublishRequest = spawnSync("curl", [
"-H", "Authorization: Bearer " + accessToken, 
"-H", "x-goog-api-verison: 2",
"-H", "Content-Length: 0",
"-X", "POST",
"-vv",
"-fail",
"https://www.googleapis.com/chromewebstore/v1.1/items/" + credentials[app_id] + "/publish"]);

process.exit(chromeWebStorePublishRequest.status);
