// Copyright © 2010 - May 2015 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

var $rv = $rv || {};

rvExtLogger = function() {
        this.PROJECT_ID = "client-side-events";
        this.DATASET_ID = "CAP_Events";
	this.EXT_SERVICE_URL = "https://www.googleapis.com/bigquery/v2/projects/PROJECT_ID/datasets/DATASET_ID/tables/TABLE_ID/insertAll";
        this.EXT_SERVICE_URL = this.EXT_SERVICE_URL.replace("PROJECT_ID", this.PROJECT_ID);
        this.EXT_SERVICE_URL = this.EXT_SERVICE_URL.replace("DATASET_ID", this.DATASET_ID);
        this.REFRESH_URL = "https://www.googleapis.com/oauth2/v3/token?client_id=1088527147109-6q1o2vtihn34292pjt4ckhmhck0rk0o7.apps.googleusercontent.com&client_secret=nlZyrcPLg6oEwO9f9Wfn29Wh&refresh_token=1/xzt4kwzE1H7W9VnKB8cAaCx6zb4Es4nKEoqaYHdTD15IgOrJDtdun6zK6XiATCKT&grant_type=refresh_token";
        this.HTTP_METHOD = "POST";
        this.INSERT = {
            "kind": "bigquery#tableDataInsertAllRequest",
            "skipInvalidRows": false,
            "ignoreUnknownValues": false,
            "rows": [
              {
                "insertId": "",
                "json": {
                  "event": "",
                  "display_id": "",
                  "ip": $rv.config.ipAddress,
                  "os": $rv.config.osName,
                  "chrome_version": /Chrome\/([0-9.]+)/.exec(navigator.appVersion)[1],
                  "cap_version": $rv.config.appVersion,
                  "ts": 0
                }
              }
            ]
        };
        this.REFRESH_DATE = 0;

	this.log = function(eventName, cb) {
          var self = this;
          if (!eventName) {return;}

          return this.refresh()
          .catch(function(err) {
            console.log("Error on external log " + err.message);
          })
          .then(function(refreshData) {
            var date = new Date(),
            year = date.getUTCFullYear(),
            month = date.getUTCMonth() + 1,
            day = date.getUTCDate(),
            insertData = self.INSERT,
            serviceUrl;

            if (month < 10) {month = "0" + month;}
            if (day < 10) {day = "0" + day;}

            serviceUrl = self.EXT_SERVICE_URL.replace("TABLE_ID", "events" + year + month + day);

            self.REFRESH_DATE = refreshData.refreshedAt || self.REFRESH_DATE;
            self.TOKEN = refreshData.token || self.TOKEN;

            insertData.rows[0].insertId = Math.random().toString(36).substr(2).toUpperCase();
            insertData.rows[0].json.event = eventName;
            insertData.rows[0].json.display_id = $rv.config.displayId || "";
            insertData.rows[0].json.ts = new Date().toISOString();

            var xhr = new XMLHttpRequest();
            xhr.open("POST", serviceUrl.replace("TABLE_ID", "events" + year + month + day), true);
            xhr.setRequestHeader("Content-Type", "application/json");
            xhr.setRequestHeader("Authorization", "Bearer " + self.TOKEN);
            xhr.send(JSON.stringify(insertData));
            if (cb) {cb()};
          });
        };

        this.refresh = function() {
          var self = this;
          return new Promise(function(resolve, reject) {
            if (new Date() - self.REFRESH_DATE < 3580000) {
              return resolve({});
            }

            var xhr = new XMLHttpRequest();
            xhr.responseType = "json";
            xhr.open("POST", self.REFRESH_URL, true);
            xhr.onloadend = function() {
              resolve({token: xhr.response.access_token, refreshedAt: new Date()});
            };
            xhr.send();
          });
        };
};

