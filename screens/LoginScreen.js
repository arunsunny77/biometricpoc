// import React, { useState, useEffect } from 'react';
// import { View, Text, TextInput, StyleSheet, Alert, Button } from 'react-native';
// import ReactNativeBiometrics, { BiometryTypes } from 'react-native-biometrics';

// const LoginScreen = ({ navigation }) => {
//   const [username, setUsername] = useState('');
//   const [password, setPassword] = useState('');
//   const rnBiometrics = new ReactNativeBiometrics();
//   const checkFaceRecognition = async () => {
    
//     const { available, biometryType } = await rnBiometrics.isSensorAvailable();

//     if (biometryType === BiometryTypes.Biometrics) {
//       //do something face id specific
//       handleFaceRecognition();
//     } else {
//       Alert.alert('Error', 'Biometrics  is not available on this device.');
//     }
//     console.log('Face is ----',biometryType);

//     if (available && biometryType === BiometryTypes.TouchID) {
//       console.log('TouchID is supported');
//     } else if (available && biometryType === BiometryTypes.FaceID) {
//       console.log('FaceID is supported');
//     } else if (available && biometryType === BiometryTypes.Biometrics) {
//       console.log('Biometrics is supported');
//     } else {
//       console.log('Biometrics not supported');
//     }
//   };

//   const handleFaceRecognition = async () => {


//     const { success } = await rnBiometrics.simplePrompt({ promptMessage: 'Confirm Biometrics' });

//     if (success) {
//       // storeCredentials()
//       Alert.alert('Authentication Success!');
//       // navigation.replace('Home');
//     } else {
//       Alert.alert('Authentication failed', 'Biometric failed. Please try again.');
//     }
//   };

//   const handleLogin = () => {
//     // Here you can add your own logic for handling username/password login
//     // This function would be called when user presses a login button
//     // For simplicity, let's assume you validate the login and proceed to biometric auth
//     checkFaceRecognition();
//   };

//   const storeCredentials = async () => {


//     rnBiometrics.createKeys()
//       .then((resultObject) => {
//         const { publicKey } = resultObject
//         console.log(publicKey)
//         sendPublicKeyToServer(publicKey)
//       })
//   };


//   return (
//     <View style={styles.container}>
//       <View style={styles.innerContainer}>
//         <Text style={styles.title}>Welcome</Text>
//         <Text style={styles.subtitle}>Login with Biometrics</Text>
//         <TextInput
//           style={styles.input}
//           placeholder="Username"
//           onChangeText={(text) => setUsername(text)}
//           value={username}
//         />
//         <TextInput
//           style={styles.input}
//           placeholder="Password"
//           onChangeText={(text) => setPassword(text)}
//           value={password}
//           secureTextEntry
//         />
//         <Text style={styles.instruction}>Please authenticate using your biometrics.</Text>
//         <Button title="Login" onPress={handleLogin} />
//       </View>
//     </View>
//   );
// };

// const styles = StyleSheet.create({
//   container: {
//     flex: 1,
//     justifyContent: 'center',
//     alignItems: 'center',
//     backgroundColor: '#000',
//   },
//   innerContainer: {
//     width: '90%',
//     padding: 20,
//     borderRadius: 10,
//     backgroundColor: '#ffffff90', // semi-transparent white background
//     shadowColor: '#000',
//     shadowOffset: { width: 0, height: 5 },
//     shadowOpacity: 0.34,
//     shadowRadius: 6.27,
//     elevation: 10,
//   },
//   title: {
//     fontSize: 34,
//     color: '#333',
//     fontWeight: 'bold',
//     marginBottom: 20,
//     textAlign: 'center',
//   },
//   subtitle: {
//     fontSize: 24,
//     color: '#555',
//     marginBottom: 20,
//     textAlign: 'center',
//   },
//   instruction: {
//     fontSize: 16,
//     color: '#777',
//     textAlign: 'center',
//     marginBottom: 20,
//   },
//   input: {
//     height: 40,
//     borderColor: '#ccc',
//     borderWidth: 1,
//     borderRadius: 5,
//     paddingHorizontal: 10,
//     marginBottom: 10,
//   },
// });

// export default LoginScreen;

import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, StyleSheet, Alert, Button } from 'react-native';
import ReactNativeBiometrics, { BiometryTypes } from 'react-native-biometrics';

const LoginScreen = ({ navigation }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [storedCredentials, setStoredCredentials] = useState(null);
  const rnBiometrics = new ReactNativeBiometrics();
  useEffect(() => {
    checkBiometricAvailability();
  }, []);

  const checkBiometricAvailability = async () => {
//     const { available, biometryType } = await rnBiometrics.isSensorAvailable();
// console.log("Biomrtic------>",biometryType)
//     if (!available) {
//       Alert.alert('Error', 'Biometrics is not available on this device.');
//     }

rnBiometrics.isSensorAvailable()
  .then((resultObject) => {
    const { available, biometryType } = resultObject
 
    if (available && biometryType === BiometryTypes.TouchID) {
      console.log('TouchID is supported')
    } else if (available && biometryType === BiometryTypes.FaceID) {
      console.log('FaceID is supported')
    } else if (available && biometryType === BiometryTypes.Biometrics) {
      console.log('Biometrics is supported')
    } else {
      console.log('Biometrics not supported')
    }
  })
  };

  const handleBiometricLogin = async () => {
    try {

      const { success } = await rnBiometrics.simplePrompt({ promptMessage: 'Confirm Biometrics' });
      // const { success } = await ReactNativeBiometrics.simplePrompt({ promptMessage: 'Confirm Biometrics' });

      if (success) {
        // Biometric authentication successful, fetch and decrypt credentials
        fetchAndDecryptCredentials();
      } else {
        Alert.alert('Authentication failed', 'Biometric authentication failed. Please try again.');
      }
    } catch (error) {
      console.error('Biometric authentication error:', error);
      Alert.alert('Error', 'Biometric authentication failed. Please try again.');
    }
  };

  const storeCredentials = async () => {
    try {
      // Check if keys already exist
      const { keysExist } = await rnBiometrics.biometricKeysExist();

      if (!keysExist) {
        // Generate keys if they don't exist
        await rnBiometrics.createKeys();
      }

      // Encrypt credentials with biometric protection
      const credentials = {
        username,
        password,
      };

      const { success } = await rnBiometrics.createSignature({
        promptMessage: 'Authenticate to store credentials',
        payload: JSON.stringify(credentials),
      });

      if (success) {
        Alert.alert('Success', 'Credentials stored securely.');
      } else {
        Alert.alert('Error', 'Failed to store credentials securely.');
      }
    } catch (error) {
      console.error('Store credentials error:', error);
      Alert.alert('Error', 'Failed to store credentials securely.');
    }
  };

  const fetchAndDecryptCredentials = async () => {
    try {
      // Fetch and decrypt credentials with biometric protection
      const { success, payload } = await rnBiometrics.createSignature({
        promptMessage: 'Authenticate to fetch credentials',
      });

      if (success) {
        const decryptedCredentials = JSON.parse(payload);
        setStoredCredentials(decryptedCredentials);
        Alert.alert('Success', 'Credentials fetched and decrypted.');
      } else {
        Alert.alert('Error', 'Failed to fetch credentials.');
      }
    } catch (error) {
      console.error('Fetch credentials error:', error);
      Alert.alert('Error', 'Failed to fetch credentials.');
    }
  };

  const handleLogin = () => {
    // Here you can add your own logic for handling username/password login
    // This function would be called when user presses a login button
    // For simplicity, let's assume you validate the login and proceed to biometric auth
    handleBiometricLogin();
  };

  return (
    <View style={styles.container}>
      <View style={styles.innerContainer}>
        <Text style={styles.title}>Welcome</Text>
        <Text style={styles.subtitle}>Login with Biometrics</Text>
        <TextInput
          style={styles.input}
          placeholder="Username"
          onChangeText={(text) => setUsername(text)}
          value={username}
        />
        <TextInput
          style={styles.input}
          placeholder="Password"
          onChangeText={(text) => setPassword(text)}
          value={password}
          secureTextEntry
        />
        <Text style={styles.instruction}>Please authenticate using your biometrics.</Text>
        <Button title="Login" onPress={handleLogin} />
        <Button title="Store Credentials" onPress={storeCredentials} />
        {storedCredentials && (
          <View style={styles.credentialsContainer}>
            <Text style={styles.credentialsText}>Stored Credentials:</Text>
            <Text>Username: {storedCredentials.username}</Text>
            <Text>Password: {storedCredentials.password}</Text>
          </View>
        )}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#000',
  },
  innerContainer: {
    width: '90%',
    padding: 20,
    borderRadius: 10,
    backgroundColor: '#ffffff90', // semi-transparent white background
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 5 },
    shadowOpacity: 0.34,
    shadowRadius: 6.27,
    elevation: 10,
  },
  title: {
    fontSize: 34,
    color: '#333',
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
  },
  subtitle: {
    fontSize: 24,
    color: '#555',
    marginBottom: 20,
    textAlign: 'center',
  },
  instruction: {
    fontSize: 16,
    color: '#777',
    textAlign: 'center',
    marginBottom: 20,
  },
  input: {
    height: 40,
    borderColor: '#ccc',
    borderWidth: 1,
    borderRadius: 5,
    paddingHorizontal: 10,
    marginBottom: 10,
  },
  credentialsContainer: {
    marginTop: 20,
    borderColor: '#ccc',
    borderWidth: 1,
    borderRadius: 5,
    padding: 10,
  },
  credentialsText: {
    fontWeight: 'bold',
    marginBottom: 10,
  },
});

export default LoginScreen;
