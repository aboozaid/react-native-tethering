import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  Pressable,
  Image,
} from 'react-native';
import HotspotScreen from './HotspotScreen';
import WifiScreen from './WifiScreen';

export default function App() {
  const [selected, setSelected] = React.useState<string | null>(null);

  if (selected === 'wifi') {
    return <WifiScreen back={() => setSelected(null)} />
  }

  if (selected === 'hotspot') {
    return <HotspotScreen back={() => setSelected(null)} />
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Image source={require('./assets/tethering-logo.webp')} style={styles.headerImg} />
        <Text style={{ fontSize: 20 }}>React Native Tethering</Text>
      </View>
      <View style={styles.buttonsContainer}>
        <Pressable style={styles.button} android_ripple={{ color: '#fff' }} onPress={() => setSelected('wifi')}>
          <Text style={styles.buttonText}>Use WiFi</Text>
        </Pressable>
        <Pressable style={styles.button} android_ripple={{ color: '#fff' }} onPress={() => setSelected('hotspot')}>
          <Text style={styles.buttonText}>Use Hotspot</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonsContainer: {
    width: '100%', 
    alignItems: 'center'
  },
  button: {
    width: '50%',
    padding: 14,
    backgroundColor: '#359962',
    borderRadius: 10,
    marginBottom: 10,
    alignItems: 'center'
  },
  buttonText: {
    color: '#fff',
    fontSize: 16
  },
  header: {
    position: 'absolute',
    top: 0
  },
  headerImg: {
    width: 200, 
    height: 200
  }
});
