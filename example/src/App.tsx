import { Text, View, StyleSheet,Button } from 'react-native';
import { multiply,initCameraScreen,LensFacing,
  FlashMode,
  Orientation,
  Mode,
  type CameraConfiguration } from 'truvideo-react-turbo-camera-sdk';

const result = multiply(3, 7);
const configuration: CameraConfiguration = {
  lensFacing: LensFacing.Front,
  flashMode: FlashMode.Off,
  orientation: Orientation.Portrait,
  outputPath: "file://\(outputPath)",
  frontResolutions: [],
  frontResolution: 'nil',
  backResolutions: [],
  backResolution: 'nil',
  mode: Mode.VideoAndPicture,
};
const initCamera =() =>{
  initCameraScreen(configuration).then((res) => {
    console.log('typeOf res', typeof res);
    console.log('res', JSON.parse(res));
    let obj = JSON.parse(res);
    console.log('filePath', obj[0].filePath);
  });
  
}

export default function App() {
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
