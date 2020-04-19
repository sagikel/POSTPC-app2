# POSTPC-app2

"honey_im_home" app in course POSTPC.

As request:
I pledge the highest level of ethical principles in support of academic excellence.
I ensure that all of my work reflects my own abilities and not those of someone else.

Answer question:

Let see SmsManager - sendTextMessage(...)
     * @param destinationAddress the address to send the message to
     * @param scAddress is the service center address or null to use
     *  the current default SMSC
     * @param text the body of the message to send
     * @param sentIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is successfully sent, or failed.
     *  The result code will be <code>Activity.RESULT_OK</code> for success,
     *  or one of these errors:<br>
     *  <code>RESULT_ERROR_GENERIC_FAILURE</code><br>
     *  <code>RESULT_ERROR_RADIO_OFF</code><br>
     *  <code>RESULT_ERROR_NULL_PDU</code><br>
     *  <code>RESULT_ERROR_NO_SERVICE</code><br>
     *  For <code>RESULT_ERROR_GENERIC_FAILURE</code> the sentIntent may include
     *  the extra "errorCode" containing a radio technology specific value,
     *  generally only useful for troubleshooting.<br>
     *  The per-application based SMS control checks sentIntent. If sentIntent
     *  is NULL the caller will be checked against all unknown applications,
     *  which cause smaller number of SMS to be sent in checking period.
     * @param deliveryIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is delivered to the recipient.  The
     *  raw pdu of the status report is in the extended data ("pdu").

     A pending intent is a wrapper around regular intent that is designed to be used by another
     application. It gives that other application the ability to perform the included action as
     it was your application with all the permissions your application has been granted.

     So we will add @param deliveryIntent to SmsManager that will be broadcast and will look
     somewhat like this:

     PendingIntent deliveryIntent = PendingIntent.getBroadcast(this, 1, new
     Intent(DELIVERY_SMS), 0);

     And that because broadcast-receiver can be filtered for action "DELIVERY_SMS" and perform
     the notification change.

     So the extra is the action : Intent(String action) to be filtered and execute the notification
     change.