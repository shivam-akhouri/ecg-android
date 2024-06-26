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
package com.github.pwittchen.neurosky.library;

import com.github.pwittchen.neurosky.library.message.BrainEvent;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

class EventBus {

  private final Subject<Object> bus = PublishSubject.create().toSerialized();

  private EventBus() {
  }

  public static EventBus create() {
    return new EventBus();
  }

  public void send(final BrainEvent object) {
    bus.onNext(object);
  }

  @SuppressWarnings("unchecked")
  public Flowable<BrainEvent> receive(BackpressureStrategy backpressureStrategy) {
    return (Flowable<BrainEvent>) (Flowable<?>) bus
        .toFlowable(backpressureStrategy)
        .filter(object -> object instanceof BrainEvent);
  }
}
