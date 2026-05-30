package com.example.nfcmuseumguide.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import com.example.nfcmuseumguide.model.Exhibit
import org.json.JSONObject

object NfcMuseumCodec {
    const val MIME_TYPE = "application/vnd.example.nfc.museum"

    fun createMessage(exhibit: Exhibit): NdefMessage {
        val payload = JSONObject()
            .put("exhibitId", exhibit.id)
            .put("title", exhibit.titleRu)
            .toString()
            .toByteArray(Charsets.UTF_8)

        return NdefMessage(arrayOf(NdefRecord.createMime(MIME_TYPE, payload)))
    }

    fun parseExhibitId(message: NdefMessage): String? {
        return message.records.firstNotNullOfOrNull { record ->
            val mime = record.toMimeType()
            if (mime != MIME_TYPE) return@firstNotNullOfOrNull null

            runCatching {
                JSONObject(String(record.payload, Charsets.UTF_8)).optString("exhibitId")
                    .takeIf { it.isNotBlank() }
            }.getOrNull()
        }
    }

    fun readExhibitId(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        return runCatching {
            ndef.connect()
            parseExhibitId(ndef.cachedNdefMessage ?: return null)
        }.also {
            runCatching { ndef.close() }
        }.getOrNull()
    }

    fun writeToTag(tag: Tag, message: NdefMessage) {
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            ndef.connect()
            try {
                if (!ndef.isWritable) error("NFC-метка защищена от записи")
                val size = message.toByteArray().size
                if (ndef.maxSize < size) error("На метке недостаточно места")
                ndef.writeNdefMessage(message)
            } finally {
                ndef.close()
            }
            return
        }

        val formatable = NdefFormatable.get(tag) ?: error("Эта метка не поддерживает NDEF")
        formatable.connect()
        try {
            formatable.format(message)
        } finally {
            formatable.close()
        }
    }
}
