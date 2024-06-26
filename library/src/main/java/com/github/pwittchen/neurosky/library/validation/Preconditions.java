/*
 * Copyright (C) 2018 Piotr Wittchen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pwittchen.neurosky.library.validation;

import android.bluetooth.BluetoothAdapter;
import com.neurosky.thinkgear.TGDevice;

public interface Preconditions {
  boolean isConnecting(TGDevice device);

  boolean isConnected(TGDevice device);

  boolean canConnect(TGDevice device);

  boolean isBluetoothAdapterInitialized();

  boolean isBluetoothAdapterInitialized(BluetoothAdapter bluetoothAdapter);

  boolean isBluetoothEnabled();

  boolean isBluetoothEnabled(BluetoothAdapter bluetoothAdapter);
}
