import React from 'react';
import { View, Text, Button, StyleSheet } from 'react-native';
import Biometry from '../nativemodule/BiometricModule';

const HomeScreen = ({ navigation }) => {
  const availabilityOption = async () => {
    Biometry.availabilityOption()
      .then(response => {
        console.log('Biometric availability:', response);
      })
      .catch(error => {
        console.error('Error checking biometric availability:', error);
      });
  };

const PASSWORD = "password"
  const setBiometric = async () => {
    Biometry.setBiometric('myPassword')
      .then(response => {
        console.log('Setting biometric authentication:', response);
      })
      .catch(error => {
        console.error('Error setting biometric authentication:', error);
      });
  };
  const verify = async () => {
    // Verify biometric authentication
    Biometry.verify()
      .then(response => {
        console.log('Verification result:', response);
      })
      .catch(error => {
        console.error('Error verifying biometric authentication:', error);
      });
  };
  const resetBiometric = async () => {
    // Reset biometric authentication
    Biometry.resetBiometric()
      .then(response => {
        console.log('Reset biometric authentication:', response);
      })
      .catch(error => {
        console.error('Error resetting biometric authentication:', error);
      });

  };
  const openSettings = async () => {
    // Open device settings
    Biometry.openSettings()
      .then(response => {
        console.log('Opening device settings:', response);
      })
      .catch(error => {
        console.error('Error opening device settings:', error);
      });

  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Home</Text>
      <Button title="availabilityOption" onPress={availabilityOption} />
      <Button title="openSettings" onPress={openSettings} />
      <Button title="verify" onPress={verify} />
      <Button title="setBiometric" onPress={setBiometric} />
      <Button title="resetBiometric" onPress={resetBiometric} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  title: {
    fontSize: 24,
    marginBottom: 20,
    textAlign: 'center',
  },
});

export default HomeScreen;
