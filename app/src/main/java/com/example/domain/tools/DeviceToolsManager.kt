package com.example.domain.tools

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

class DeviceToolsManager(private val context: Context) {

    data class ToolResult(
        val success: Boolean,
        val message: String,
        val requiresPermission: String? = null
    )

    fun openApp(packageName: String): ToolResult {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                ToolResult(true, "Opened $packageName successfully.")
            } else {
                ToolResult(false, "Application '$packageName' is not installed on this device.")
            }
        } catch (e: Exception) {
            ToolResult(false, "Failed to open app: ${e.localizedMessage}")
        }
    }

    fun searchAndCallContact(contactName: String): ToolResult {
        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val hasContactsPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        var phoneNumber: String? = null

        if (hasContactsPermission) {
            try {
                val cursor = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    ),
                    "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                    arrayOf("%$contactName%"),
                    null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (numberIndex >= 0) {
                            phoneNumber = it.getString(numberIndex)
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        return try {
            if (phoneNumber != null && hasCallPermission) {
                val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(callIntent)
                ToolResult(true, "Calling $contactName ($phoneNumber)...")
            } else if (phoneNumber != null) {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                ToolResult(true, "Opened dialer for $contactName.")
            } else {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                ToolResult(true, "Contact '$contactName' not found directly. Opened dialer.")
            }
        } catch (e: Exception) {
            ToolResult(false, "Could not initiate call: ${e.localizedMessage}")
        }
    }

    fun sendWhatsAppMessage(contactNameOrPhone: String, message: String): ToolResult {
        return try {
            val uri = Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(message))
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                ToolResult(true, "Opened WhatsApp to send: \"$message\"")
            } else {
                // Fallback to general share intent
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Send message via").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                ToolResult(true, "WhatsApp not installed, opened standard share.")
            }
        } catch (e: Exception) {
            ToolResult(false, "Failed to prepare message: ${e.localizedMessage}")
        }
    }

    fun sendGmail(recipientEmail: String, subject: String, body: String): ToolResult {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                if (recipientEmail.isNotBlank()) {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                }
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                ToolResult(true, "Prepared email to '$recipientEmail'.")
            } else {
                ToolResult(false, "No compatible email client found on this device.")
            }
        } catch (e: Exception) {
            ToolResult(false, "Failed to draft email: ${e.localizedMessage}")
        }
    }
}
