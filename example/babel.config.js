// const path = require('path');
// const pak = require('../packages/wifi/package.json');

// const aliases = fs.readdirSync(packages)
//   .filter((name) => !name.startsWith('.'))
//   .forEach((name) => {
//     const pak = require(`../packages/${name}/package.json`);

//     if (pak.source == null) {
//       return;
//     }
    
//     return { [pak.name]: path.resolve(packages, name) }
//   });

//   console.log(aliases);
module.exports = {
  presets: ['module:metro-react-native-babel-preset'],
  // plugins: [
  //   [
  //     'module-resolver',
  //     {
  //       extensions: ['.tsx', '.ts', '.js', '.json'],
  //       alias: {
  //         "@react-native-tethering/wifi": path.join(__dirname, '../packages/wifi/', pak.source),
  //       },
  //     },
  //   ],
  // ],
};
