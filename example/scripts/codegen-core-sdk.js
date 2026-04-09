#!/usr/bin/env node
/**
 * Runs codegen for @trunpm/truvideo-react-turbo-core-sdk and applies the package-name patch
 * so NativeTruVideoReactTurboCoreSdkSpec is in com.truvideoreactturbocoresdk.
 * Run from example/ dir.
 */
const path = require('path');
const { execSync } = require('child_process');
const fs = require('fs');

const exampleDir = path.resolve(__dirname, '..');
const coreSdkDir = path.join(
  exampleDir,
  'node_modules',
  '@trunpm',
  'truvideo-react-turbo-core-sdk'
);

if (!fs.existsSync(coreSdkDir)) {
  console.warn('@trunpm/truvideo-react-turbo-core-sdk not found, skipping codegen');
  process.exit(0);
}

// 1) React Native codegen (creates android/generated/jni and java/com/facebook/fbreact/specs)
execSync(
  'npx react-native codegen --path node_modules/@trunpm/truvideo-react-turbo-core-sdk',
  {
  cwd: exampleDir,
  stdio: 'inherit',
  }
);

// 2) Bob's patchCodegen: move Java from com.facebook.fbreact.specs -> codegenConfig.android.javaPackageName
const { patchCodegen } = require(path.join(exampleDir, 'node_modules', 'react-native-builder-bob', 'lib', 'utils', 'patchCodegen.js'));
const packageJson = require(path.join(coreSdkDir, 'package.json'));
const report = { info: () => {}, warn: () => {}, success: () => {}, error: (m) => { throw new Error(m); } };

patchCodegen(coreSdkDir, packageJson, report)
  .catch((err) => {
    if (err.message && err.message.includes('Skipping')) return;
    console.error(err);
    process.exit(1);
  });
