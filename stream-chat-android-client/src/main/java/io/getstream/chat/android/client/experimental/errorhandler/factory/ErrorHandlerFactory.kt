/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.experimental.errorhandler.factory

import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Factory used to provide an [ErrorHandler] that will be used to handle plugins' errors.
 *
 * @see [io.getstream.chat.android.client.experimental.plugin.Plugin]
 */
@InternalStreamChatApi
public interface ErrorHandlerFactory {

    /**
     * Provides a single instance of [ErrorHandler].
     *
     * @return The [ErrorHandler] instance.
     */
    public fun create(): ErrorHandler
}
