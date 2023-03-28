import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  Pressable,
  ToastAndroid,
  ScrollView,
} from 'react-native';
import TetheringManager, { Network, Event, TetheringError } from '@react-native-tethering/wifi';

type WifiScreenProps = {
  back: () => void
}

export default function WifiScreen({ back }: WifiScreenProps) {
  const [networks, setNetworks] = React.useState<Network[]>([]);

  React.useEffect(() => {
    const subscriber = TetheringManager.addEventListener(Event.OnNetworkDisconnected, () => {
      // disconnected from the network
    })

    return () => subscriber.remove();
  }, []);

  return (
    <ScrollView>
      <Pressable style={styles.backButton} android_ripple={{color: '#fff' }} onPress={back}>
        <Text>Back to home</Text>
      </Pressable>
      <View style={styles.wrapper}>
        <Text style={styles.wrapperHeader}>WiFi States</Text>
        <View style={styles.row}>
          <Pressable
            style={styles.button}
            android_ripple={{ color: '#ccc' }}
            onPress={async () => {
              try {
                const state = await TetheringManager.isWifiEnabled();
                ToastAndroid.show(`WiFi state: ${state}`, ToastAndroid.SHORT)
              } catch (error) {
                if (error instanceof TetheringError) {
                  ToastAndroid.show(error.message, ToastAndroid.LONG)
                }
                console.log(error);
              }
            }}
          >
            <Text style={{ color: '#000' }}>Check WiFi Enabled</Text>
          </Pressable>
          <Pressable
            style={styles.button}
            android_ripple={{ color: '#ccc' }}
            onPress={async () => {
              try {
                await TetheringManager.setWifiEnabled();
              } catch (error) {
                if (error instanceof TetheringError) {
                  ToastAndroid.show(error.message, ToastAndroid.LONG)
                }
                console.log(error);
              }
            }}
          >
            <Text style={{ color: '#000' }}>Enable WiFi</Text>
          </Pressable>
          <Pressable
            style={styles.button}
            android_ripple={{ color: '#ccc' }}
            onPress={async () => {
              try {
                await TetheringManager.setWifiDisabled();
              } catch (error) {
                if (error instanceof TetheringError) {
                  ToastAndroid.show(error.message, ToastAndroid.LONG)
                }
                console.log(error);
              }
            }}
          >
            <Text style={{ color: '#000' }}>Disable WiFi</Text>
          </Pressable>
        </View>
      </View>
      <View style={styles.wrapper}>
        <Text style={styles.wrapperHeader}>WiFi Connections</Text>
        <View style={{ width: '100%' }}>
          <Pressable
          style={styles.button}
          android_ripple={{ color: '#ccc' }}
          onPress={async () => {
            try {
              await TetheringManager.saveNetworkInDevice({
                ssid: 'network name',
                password: 'network password'
              });
              ToastAndroid.show('Network Saved', ToastAndroid.SHORT)
            } catch (error) {
              if (error instanceof TetheringError) {
                ToastAndroid.show(error.message, ToastAndroid.LONG)
              }
              console.log(error);
            }
          }}
        >
          <Text style={{ color: '#000' }}>Save a network in device</Text>
          </Pressable>
          <Pressable
            style={styles.button}
            android_ripple={{ color: '#ccc' }}
            onPress={async () => {
              try {
                await TetheringManager.connectToLocalNetwork({
                  ssid: 'network name',
                  password: 'network password',
                  isHidden: true
                });
                ToastAndroid.show('WiFi Connected', ToastAndroid.SHORT)
              } catch (error) {
                if (error instanceof TetheringError) {
                  ToastAndroid.show(error.message, ToastAndroid.LONG)
                }
                console.log(error);
              }
            }}
          >
            <Text style={{ color: '#000' }}>Connect to local network</Text>
          </Pressable>
          <Pressable
            style={styles.button}
            android_ripple={{ color: '#ccc' }}
            onPress={async () => {
              try {
                await TetheringManager.connectToNetwork({
                  ssid: 'network name',
                  password: 'network password',
                  isHidden: true
                });
                ToastAndroid.show('WiFi Connected', ToastAndroid.SHORT)
              } catch (error) {
                if (error instanceof TetheringError) {
                  ToastAndroid.show(error.message, ToastAndroid.LONG)
                }
                console.log(error);
              }
            }}
          >
            <Text style={{ color: '#000' }}>Connect to a network</Text>
          </Pressable>
        </View>
      </View>
      
      <View style={styles.wrapper}>
        <Text style={styles.wrapperHeader}>WiFi Disconnect</Text>
        <View style={{ width: '100%' }}>
        <Pressable
        style={styles.button}
        android_ripple={{ color: '#ccc' }}
        onPress={async () => {
          try {
            await TetheringManager.disconnectFromLocalNetwork()
            ToastAndroid.show('WiFi Disconnected', ToastAndroid.SHORT)
          } catch (error) {
            if (error instanceof TetheringError) {
              ToastAndroid.show(error.message, ToastAndroid.LONG)
            }
            console.log(error);
          }
        }}
      >
        <Text style={{ color: '#000' }}>Disconnect from a local network</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#ccc' }}
        onPress={async () => {
          try {
            await TetheringManager.disconnectFromNetwork()
          } catch (error) {
            if (error instanceof TetheringError) {
              ToastAndroid.show(error.message, ToastAndroid.LONG)
            }
            console.log(error);
          }
        }}
      >
        <Text style={{ color: '#000' }}>Disconnect from a network</Text>
      </Pressable>
        </View>
      </View>
      
      <View style={styles.wrapper}>
        <Text style={styles.wrapperHeader}>WiFi Helpers</Text>
        <View style={{ width: '100%' }}>
        <Pressable
        style={styles.button}
        android_ripple={{ color: '#ccc' }}
        onPress={async () => {
          try {
            const wifiNetworks = await TetheringManager.getWifiNetworks(true);
            setNetworks(wifiNetworks);
          } catch (error: any) {
            if (error instanceof TetheringError) {
              ToastAndroid.show(error.message, ToastAndroid.LONG)
            }
            console.log(error);
          }
        }}
      >
        <Text style={{ color: '#000' }}>Get list of networks</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#ccc' }}
        onPress={async () => {
          try {
            const maxSuggestions = await TetheringManager.getMaxNumberOfNetworkSuggestions()
            ToastAndroid.show(`${maxSuggestions} Max suggestions per app`, ToastAndroid.SHORT)
          } catch (error: any) {
            if (error instanceof TetheringError) {
              ToastAndroid.show(error.message, ToastAndroid.LONG)
            }
            console.log(error);
          }
        }}
      >
        <Text style={{ color: '#000' }}>Get max suggestions per app</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#ccc' }}
        onPress={async () => {
          try {
            const ip = await TetheringManager.getDeviceIP()
            ToastAndroid.show(`Current Device IP: ${ip}`, ToastAndroid.SHORT)
            console.log(ip)
          } catch (error: any) {
            if (error instanceof TetheringError) {
              ToastAndroid.show(error.message, ToastAndroid.LONG)
            }
            console.log(error);
          }
        }}
      >
        <Text style={{ color: '#000' }}>Get device ip</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#ccc' }}
        onPress={async () => {
          try {
            const status = await TetheringManager.isDeviceAlreadyConnected();
            ToastAndroid.show(`Current WiFi status: ${status}`, ToastAndroid.SHORT)
          } catch (error: any) {
            if (error instanceof TetheringError) {
              ToastAndroid.show(error.message, ToastAndroid.LONG)
            }
            console.log(error);
          }
        }}
      >
        <Text style={{ color: '#000' }}>Check if connected to a network</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#ccc' }}
        onPress={() => {
          TetheringManager.openWifiSettings(false)
        }}
      >
        <Text style={{ color: '#000' }}>Open wifi settings</Text>
      </Pressable>
        </View>
      </View>
      

      <View style={{ marginVertical: 15 }}>
        {networks.map((n, i) => (
          <View
            style={styles.networksWrapper}
            key={i}
          >
            <Text style={{ color: '#000' }}>SSID: {n.ssid}</Text>
            <Text style={{ color: '#000' }}>BSSID: {n.bssid}</Text>
            <Text style={{ color: '#000' }}>capabilities: {n.capabilities}</Text>
            <Text style={{ color: '#000' }}>frequency: {n.frequency}</Text>
            <Text style={{ color: '#000' }}>level: {n.level}</Text>
            <Text style={{ color: '#000' }}>timestamp: {n.timestamp}</Text>
          </View>
        ))}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  wrapper: { 
    backgroundColor: '#359962',
    padding: 15,
    marginBottom: 8
  },
  wrapperHeader: { 
    fontSize: 16, 
    color: '#fff', 
    marginBottom: 12 
  },
  row: {
    flexDirection: 'row', 
    justifyContent: 'space-between', 
    width: '100%' 
  },
  button: {
    padding: 14,
    backgroundColor: '#fff',
    borderRadius: 10,
    marginBottom: 10,
    alignItems: 'center'
  },
  networksWrapper: {
    padding: 12, 
    backgroundColor: '#ccc', 
    marginVertical: 6
  },
  backButton: {
    backgroundColor: '#ddd', 
    padding: 15, 
    alignItems: 'center'
  }
});
