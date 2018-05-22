'use strict';

const AutoStartPermissions = require('react-native').NativeModules.AutoStartPermissions;

const platform = require('react-native').Platform.OS;

module.exports = {
  check: function () {
    return new Promise(function (resolve, reject) {
      if (platform === 'ios') {
        resolve(true);
      } else {
        AutoStartPermissions.check(
          isChecked => {
            if (isChecked) {
              resolve(true);
            } else {
              reject();
            }
          }
        )
      }
    });
  }
};
