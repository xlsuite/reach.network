// Copyright © 2010 - May 2015 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

(function() {
  var EXTERNAL_LOGGER_SERVICE_URL = "https://www.googleapis.com/bigquery/v2/projects/client-side-events/datasets/Viewer_Events/tables/TABLE_ID/insertAll";

  var EXTERNAL_LOGGER_REFRESH_URL = "https://www.googleapis.com/oauth2/v3/token?client_id=1088527147109-6q1o2vtihn34292pjt4ckhmhck0rk0o7.apps.googleusercontent.com&client_secret=nlZyrcPLg6oEwO9f9Wfn29Wh&refresh_token=1/xzt4kwzE1H7W9VnKB8cAaCx6zb4Es4nKEoqaYHdTD15IgOrJDtdun6zK6XiATCKT&grant_type=refresh_token";

  var EXTERNAL_LOGGER_INSERT_SCHEMA = {
    "kind": "bigquery#tableDataInsertAllRequest",
    "skipInvalidRows": false,
    "ignoreUnknownValues": false,
    "rows": [
      {
        "insertId": "",
        "json": {
          "event": "",
          "display_id": "",
          "viewer_version": "",
          "event_details": "",
          "ts": 0
        }
      }
    ]
  };

  var EXTERNAL_LOGGER_REFRESH_DATE = 0;
  var EXTERNAL_LOGGER_TOKEN = "";

  function logExternal(eventName, displayId, version, eventDetails) {
    if (!eventName) {return;}

    return refreshToken(insertWithToken);

    function insertWithToken(refreshData) {
      var date = new Date(),
      year = date.getUTCFullYear(),
      month = date.getUTCMonth() + 1,
      day = date.getUTCDate(),
      insertData = JSON.parse(JSON.stringify(EXTERNAL_LOGGER_INSERT_SCHEMA)),
      serviceUrl;

      if (month < 10) {month = "0" + month;}
      if (day < 10) {day = "0" + day;}

      serviceUrl = EXTERNAL_LOGGER_SERVICE_URL.replace
      ("TABLE_ID", "events" + year + month + day);

      EXTERNAL_LOGGER_REFRESH_DATE = refreshData.refreshedAt || EXTERNAL_LOGGER_REFRESH_DATE;
      EXTERNAL_LOGGER_TOKEN = refreshData.token || EXTERNAL_LOGGER_TOKEN;

      insertData.rows[0].insertId = Math.random().toString(36).substr(2).toUpperCase();
      insertData.rows[0].json.event = eventName;
      insertData.rows[0].json.display_id = displayId;
      insertData.rows[0].json.viewer_version = version;
      if (eventDetails) {insertData.rows[0].json.event_details = eventDetails;}
      insertData.rows[0].json.ts = new Date().toISOString();

      var xhr = new XMLHttpRequest();
      xhr.open("POST", serviceUrl, true);
      xhr.setRequestHeader("Content-Type", "application/json");
      xhr.setRequestHeader("Authorization", "Bearer " + EXTERNAL_LOGGER_TOKEN);
      xhr.send(JSON.stringify(insertData));
    }
  }

  function refreshToken(cb) {
    if (new Date() - EXTERNAL_LOGGER_REFRESH_DATE < 3580000) {
      return cb({});
    }

    var xhr = new XMLHttpRequest();
    xhr.open("POST", EXTERNAL_LOGGER_REFRESH_URL, true);
    xhr.onloadend = function() {
      var resp = JSON.parse(xhr.response);
      cb({token: resp.access_token, refreshedAt: new Date()});
    };
    xhr.send();
  }

  window.logExternal = logExternal;
}());
