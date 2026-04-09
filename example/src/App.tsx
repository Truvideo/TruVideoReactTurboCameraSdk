import { Text, View, StyleSheet, Button } from 'react-native';
import React, { useEffect, useState } from 'react';
import {
  LensFacing,
  FlashMode,
  initCameraScreen,
  Orientation,
  CameraMode,
} from '@trunpm/truvideo-react-turbo-camera-sdk';
import type { CameraConfiguration } from '@trunpm/truvideo-react-turbo-camera-sdk';
import QuickCrypto from 'react-native-quick-crypto';
import {
  isAuthenticated,
  isAuthenticationExpired,
  generatePayload,
  authenticate,
  initAuthentication,
  clearAuthentication,
} from '@trunpm/truvideo-react-turbo-core-sdk';


const result = 3;
const configuration: CameraConfiguration = {
  lensFacing: LensFacing.Front,
  flashMode: FlashMode.Off,
  orientation: Orientation.Portrait,
  outputPath: '',
  frontResolutions: [],
  frontResolution: null,
  backResolutions: [],
  backResolution: null,
  mode: CameraMode.singleImage(),
};

const initCamera = () => {
  initCameraScreen(configuration).then((res) => {
    console.log('typeOf res', typeof res);
    console.log('res', res);
    if (res && Array.isArray(res) && res[0] && res[0].filePath) {
      console.log('filePath', res[0].filePath);
    } else {
      console.log('filePath not available');
    }
  });

  // videoAndImage().then((res) => {
  //   console.log('typeOf res', typeof res);
  //   console.log('res', JSON.parse(res));
  //   let obj = JSON.parse(res);
  //   console.log('filePath', obj[0].filePath);
  // });
};

export default function App() {
  const [apiKey, setApiKey] = useState('EPhPPsbv7e');
  const [secretKey, setSecretKey] = useState('9lHCnkfeLl');
    useEffect(() => {
         // clearAuth();
        authFunc();
    }, []);


    const authFunc = async () => {
          try {
              
              const isAuth = await isAuthenticated();
              // Check if authentication token has expired
              const isAuthExpired = await isAuthenticationExpired();
              //generate payload for authentication
              const payload = await generatePayload();
              const signature = await toSha256String(secretKey, payload);
              // Authenticate user
              if (!isAuth || isAuthExpired) {
                  await authenticate(apiKey, payload, signature, '');
              }
              // If user is authenticated successfully
              const initAuth = await initAuthentication();
      
              console.log('initAuth', initAuth);
          } catch (error) {
              console.log('error', error);
          }
      };

    const toSha256String = (signature: any, payload: any) => {
            try {
                // Create HMAC using 'sha256' and the provided signature as the key
                const hmac = QuickCrypto.createHmac('sha256', signature);
                // Update the HMAC with the payload
                hmac.update(payload);
                // Generate the HMAC digest and convert it to a hex string
                const hash = hmac.digest('hex');
                return hash;
            } catch (error) {
                console.error('Error generating SHA256 string:', error);
                return '';
            }
        };
  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
     
      <Button
        onPress={() => initCamera()}
        title="Press to initialize camera"
        color="#eb4034"
        accessibilityLabel="Learn more about this purple button"
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});

