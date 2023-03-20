const path = require('path');
const fs = require('fs');
// const pak = require('../package.json');
const packages = path.resolve(__dirname, '..', 'packages');

const dependencies = {}

fs.readdirSync(packages)
  .filter((name) => !name.startsWith('.'))
  .forEach((name) => {
    const pak = require(`../packages/${name}/package.json`);

    if (pak.source == null) {
      return;
    }
    
    dependencies[pak.name] = { root: path.resolve(packages, name) }
  });

  
module.exports = {
  dependencies,
};