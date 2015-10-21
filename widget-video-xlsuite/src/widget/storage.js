/* global config */

var RiseVision = RiseVision || {};
RiseVision.Video = RiseVision.Video || {};

RiseVision.Video.Storage = function (data) {
  "use strict";

  var _initialLoad = true;

  /*
   *  Public Methods
   */
  function init() {
    var storage = document.getElementById("videoStorage");

    if (!storage) {
      return;
    }

    storage.addEventListener("rise-storage-response", function(e) {
      if (e.detail && e.detail.url) {

        if (_initialLoad) {
          _initialLoad = false;

          RiseVision.Video.onStorageInit(e.detail.url);
        }
        else {
          // check for "changed" property and ensure it is true
          if (e.detail.hasOwnProperty("changed") && e.detail.changed) {
            RiseVision.Video.onStorageRefresh(e.detail.url);
          }
        }
      }
    });

    storage.addEventListener("rise-storage-no-file", function() {
      RiseVision.Video.noStorageFile();
    });

    storage.setAttribute("folder", data.storage.folder);
    storage.setAttribute("fileName", data.storage.fileName);
    storage.setAttribute("companyId", data.storage.companyId);
    storage.setAttribute("env", config.STORAGE_ENV);
    storage.go();
  }

  return {
    "init": init
  };
};
