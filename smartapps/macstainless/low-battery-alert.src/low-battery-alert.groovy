/**
 *  Battery Tester
 *
 */

definition(
name: "Low Battery Alert",
namespace: "macstainless",
author: "Aaron",
description: "this app will poll chosen devices that use a battery and send an alert when it's low",
category: "My Apps",
iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
preferences {
	section("Battery Alarm Level") {
		input "alarmAt", "number", title: "Alert when below...", required: true
        input "batteryDevices", "capability.battery", title: "Which devices?", multiple: true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unschedule()

	initialize()
}

def initialize() {

    //schedule the job
    schedule("0 0 10am 1,15 * ?", doBatteryCheck)

    //run at install too
    doBatteryCheck()

}

def doBatteryCheck() {

	def belowLevelCntr = 0

    def pushMsg = ""

	for (batteryDevice in batteryDevices) {

    	def batteryLevel = batteryDevice.currentValue("battery")

        if ( batteryLevel <= settings.alarmAt.toInteger() ) {

            pushMsg += "${batteryDevice.name} named ${batteryDevice.label} is at: ${batteryLevel}% \n"

            belowLevelCntr++
        }
    }

    if ( belowLevelCntr ){

    	pushMsg = "You have ${belowLevelCntr} devices below the set alarm level. \n" + pushMsg

    } else {

        pushMsg = "Battery Check App executed with no devices below alarm level"
    }

    log.debug(pushMsg)

    sendPush(pushMsg)
}