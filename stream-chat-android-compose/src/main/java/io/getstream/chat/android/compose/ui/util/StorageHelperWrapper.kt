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

package io.getstream.chat.android.compose.ui.util

import android.content.Context
import android.net.Uri
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.StorageHelper
import io.getstream.chat.android.client.models.Attachment

/**
 * Wrapper around the [StorageHelper] class, with some extra functionality that makes it easier to
 * decouple the business logic and make it consistent.
 *
 * @param context The context of the app, used to fetch files and media.
 * @param storageHelper The storage helper that provides all the logic required to work with the
 * system storage.
 */
public class StorageHelperWrapper(
    private val context: Context,
    private val storageHelper: StorageHelper,
) {

    /**
     * Loads a list of file metadata from the system.
     *
     * @return List of [AttachmentMetaData] that describe the files.
     */
    public fun getFiles(): List<AttachmentMetaData> = storageHelper.getFileAttachments(context)

    /**
     * Loads a list of media metadata from the system.
     *
     * @return List of [AttachmentMetaData] that describe the files.
     */
    public fun getMedia(): List<AttachmentMetaData> = storageHelper.getMediaAttachments(context)

    /**
     * Transforms a list of [AttachmentMetaData] into a list of [Attachment]s. This is required
     * because we need to prepare the files for upload.
     *
     * @param attachments The list of attachment meta data that we transform.
     * @return List of [Attachment]s that we will upload.
     */
    public fun getAttachmentsForUpload(attachments: List<AttachmentMetaData>): List<Attachment> {
        return getAttachmentsFromMetaData(attachments)
    }

    /**
     * Loads attachment files from the provided metadata, so that we can upload them.
     *
     * @param metaData The list of attachment meta data that we transform.
     * @return List of [Attachment]s with files prepared for uploading.
     */
    private fun getAttachmentsFromMetaData(metaData: List<AttachmentMetaData>): List<Attachment> {
        return metaData.map {
            val fileFromUri = storageHelper.getCachedFileFromUri(context, it)

            Attachment(
                upload = fileFromUri,
                type = it.type,
                name = it.title ?: fileFromUri.name ?: "",
                fileSize = it.size.toInt(),
                mimeType = it.mimeType
            )
        }
    }

    /**
     * Takes a list of file Uris and transforms them into a list of [Attachment]s so that we can
     * upload them.
     *
     * @param uris Selected file Uris, to be transformed.
     * @return List of [Attachment]s with files prepared for uploading.
     */
    public fun getAttachmentsFromUris(uris: List<Uri>): List<Attachment> {
        return getAttachmentsFromMetaData(storageHelper.getAttachmentsFromUriList(context, uris))
    }
}
