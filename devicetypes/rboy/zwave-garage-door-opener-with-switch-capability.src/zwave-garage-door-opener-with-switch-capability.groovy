/* **DISCLAIMER**
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * Without limitation of the foregoing, Contributors/Regents expressly does not warrant that:
 * 1. the software will meet your requirements or expectations;
 * 2. the software or the software content will be free of bugs, errors, viruses or other defects;
 * 3. any results, output, or data provided through or generated by the software will be accurate, up-to-date, complete or reliable;
 * 4. the software will be compatible with third party software;
 * 5. any errors in the software will be corrected.
 * The user assumes all responsibility for selecting the software and for the results obtained from the use of the software. The user shall bear the entire risk as to the quality and the performance of the software.
 */ 
 
/*
 * Modified by RBoy, SmartThings Z-Wave Garage Door Opener base code as of 2015-9-19
 * Changes Copyright RBoy, redistribution of any changes or modified code is not allowed without permission
 * Version 2.1.2
 *
 * Change Log
 * 2015-9-23 - Updated layout and colors
 * 2015-9-19 - Updated to MultiAttribute Tiles
 * 2015-7-7 - Forced update GUI on sensor battery state change
 * 2015-7-6 - Added support for low battery notification and tile
 * 2015-4-29 - Added Garage door control capability
 * 2015-2-22 - Fixed issue with swtich status not being reported
 * 2015-2-2 - Added support for Switch capabilities to use directly with ST App
 *
 */
 
 /**
 *  Z-Wave Garage Door Opener
 *
 *  Copyright 2014 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Z-Wave Garage Door Opener with Switch Capability", author: "RBoy") {
		capability "Actuator"
		capability "Door Control"
		capability "Garage Door Control"
		capability "Contact Sensor"
		capability "Refresh"
		capability "Sensor"
        capability "Switch"
        capability "Momentary"
        capability "Battery"
        
        attribute "lowBattery", "string"

		fingerprint deviceId: "0x4007", inClusters: "0x98"
		fingerprint deviceId: "0x4006", inClusters: "0x98"
	}

	simulator {
		status "closed": "command: 9881, payload: 00 66 03 00"
		status "opening": "command: 9881, payload: 00 66 03 FE"
		status "open": "command: 9881, payload: 00 66 03 FF"
		status "closing": "command: 9881, payload: 00 66 03 FC"
		status "unknown": "command: 9881, payload: 00 66 03 FD"

		reply "988100660100": "command: 9881, payload: 00 66 03 FC"
		reply "9881006601FF": "command: 9881, payload: 00 66 03 FE"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"summary", type: "generic", width: 6, height: 4){
			tileAttribute ("device.door", key: "PRIMARY_CONTROL") {
                attributeState("unknown", label:'${name}', action:"refresh.refresh", icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e")
                attributeState("closed", label:'${name}', action:"door control.open", icon:"st.doors.garage.garage-closed", backgroundColor:"#79b821", nextState:"opening")
                attributeState("open", label:'${name}', action:"door control.close", icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e", nextState:"closing")
                attributeState("opening", label:'${name}', icon:"st.doors.garage.garage-opening", backgroundColor:"#ffe71e")
                attributeState("closing", label:'${name}', icon:"st.doors.garage.garage-closing", backgroundColor:"#ffe71e")
            }
            tileAttribute ("device.lowBattery", key: "SECONDARY_CONTROL") {
	            attributeState "battery", label:'${currentValue}', backgroundColor:"#ffffff"
            }
        }
		standardTile("toggle", "device.door", width: 4, height: 4) {
			state("unknown", label:'${name}', action:"refresh.refresh", icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', action:"door control.open", icon:"st.doors.garage.garage-closed", backgroundColor:"#79b821", nextState:"opening")
			state("open", label:'${name}', action:"door control.close", icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e", nextState:"closing")
			state("opening", label:'${name}', icon:"st.doors.garage.garage-opening", backgroundColor:"#ffe71e")
			state("closing", label:'${name}', icon:"st.doors.garage.garage-closing", backgroundColor:"#ffe71e")
		}
		standardTile("open", "device.door", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'open', action:"door control.open", icon:"st.doors.garage.garage-opening"
		}
		standardTile("close", "device.door", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'close', action:"door control.close", icon:"st.doors.garage.garage-closing"
		}
		standardTile("refresh", "device.door", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("batteryState", "device.lowBattery", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "battery", label:'${currentValue}', backgroundColor:"#ffffff"
        }
        valueTile("battery", "device.battery", width: 2, height: 2, inactiveLabel: false) {
            state "battery", label:'Sensor Battery\n${currentValue}%', unit: "", backgroundColors:[
                [value: 15, color: "#ff0000"],
                [value: 99, color: "#ffffff"]
            ]
        }

		main "summary"
		details(["summary", "open", "close", "battery", "refresh"])
	}
}

import physicalgraph.zwave.commands.barrieroperatorv1.*

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
		if (state.sec) {
			result = createEvent(descriptionText:description, displayed:false)
		} else {
			result = createEvent(
				descriptionText: "This device failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				displayed: true,
			)
		}
	} else {
		def cmd = zwave.parse(description, [ 0x98: 1, 0x72: 2 ])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	log.debug "\"$description\" parsed to ${result.inspect()}"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x71: 3, 0x80: 1, 0x85: 2, 0x63: 1, 0x98: 1])
	log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
	createEvent(name:"secureInclusion", value:"success", descriptionText:"Secure inclusion was successful")
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	state.sec = cmd.commandClassSupport.collect { String.format("%02X ", it) }.join()
	if (cmd.commandClassControl) {
		state.secCon = cmd.commandClassControl.collect { String.format("%02X ", it) }.join()
	}
	log.debug "Security command classes: $state.sec"
	createEvent(name:"secureInclusion", value:"success", descriptionText:"$device.displayText is securely included")
}

def zwaveEvent(BarrierOperatorReport cmd) {
	log.debug "BarrierOperatorReport $cmd"
	def result = []
	def map = [ name: "door" ]
	switch (cmd.barrierState) {
		case BarrierOperatorReport.BARRIER_STATE_CLOSED:
			map.value = "closed"
			result << createEvent(name: "contact", value: "closed", displayed: false)
			result << createEvent(name: "switch", value: "off", displayed: false)
			break
		case BarrierOperatorReport.BARRIER_STATE_UNKNOWN_POSITION_MOVING_TO_CLOSE:
			map.value = "closing"
			break
		case BarrierOperatorReport.BARRIER_STATE_UNKNOWN_POSITION_STOPPED:
			map.descriptionText = "$device.displayName door state is unknown"
			map.value = "unknown"
			break
		case BarrierOperatorReport.BARRIER_STATE_UNKNOWN_POSITION_MOVING_TO_OPEN:
			map.value = "opening"
			result << createEvent(name: "contact", value: "open", displayed: false)
			result << createEvent(name: "switch", value: "on", displayed: false)
			break
		case BarrierOperatorReport.BARRIER_STATE_OPEN:
			map.value = "open"
			result << createEvent(name: "contact", value: "open", displayed: false)
			result << createEvent(name: "switch", value: "on", displayed: false)
			break
	}
	result + createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []
	def map = [:]
	if (cmd.notificationType == 6) {
		map.displayed = true
		switch(cmd.event) {
			case 0x40:
				if (cmd.eventParameter[0]) {
					map.descriptionText = "$device.displayName performing initialization process"
				} else {
					map.descriptionText = "$device.displayName initialization process complete"
				}
				break
			case 0x41:
				map.descriptionText = "$device.displayName door operation force has been exceeded"
				break
			case 0x42:
				map.descriptionText = "$device.displayName motor has exceeded operational time limit"
				break
			case 0x43:
				map.descriptionText = "$device.displayName has exceeded physical mechanical limits"
				break
			case 0x44:
				map.descriptionText = "$device.displayName unable to perform requested operation (UL requirement)"
				break
			case 0x45:
				map.descriptionText = "$device.displayName remote operation disabled (UL requirement)"
				break
			case 0x46:
				map.descriptionText = "$device.displayName failed to perform operation due to device malfunction"
				break
			case 0x47:
				if (cmd.eventParameter[0]) {
					map.descriptionText = "$device.displayName vacation mode enabled"
				} else {
					map.descriptionText = "$device.displayName vacation mode disabled"
				}
				break
			case 0x48:
				if (cmd.eventParameter[0]) {
					map.descriptionText = "$device.displayName safety beam obstructed"
				} else {
					map.descriptionText = "$device.displayName safety beam obstruction cleared"
				}
				break
			case 0x49:
				if (cmd.eventParameter[0]) {
					map.descriptionText = "$device.displayName door sensor ${cmd.eventParameter[0]} not detected"
				} else {
					map.descriptionText = "$device.displayName door sensor not detected"
				}
				break
			case 0x4A:
				if (cmd.eventParameter[0]) {
					map.descriptionText = "$device.displayName door sensor ${cmd.eventParameter[0]} has a low battery"
				} else {
					map.descriptionText = "$device.displayName door sensor has a low battery"
				}
                sendEvent(name: "lowBattery", value: "Replace Sensor Battery", descriptionText: map.descriptionText, displayed: true, isStateChange: true) // Register low Battery
				result << createEvent(name: "battery", value: 1, unit: "%", descriptionText: map.descriptionText)
				break
			case 0x4B:
				map.descriptionText = "$device.displayName detected a short in wall station wires"
				break
			case 0x4C:
				map.descriptionText = "$device.displayName is associated with non-Z-Wave remote control"
				break
			default:
				map.descriptionText = "$device.displayName: access control alarm $cmd.event"
				map.displayed = false
				break
		}
	} else if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 1:
			case 2:
				map.descriptionText = "$device.displayName detected intrusion"
				break
			case 3:
				map.descriptionText = "$device.displayName tampering detected: product cover removed"
				break
			case 4:
				map.descriptionText = "$device.displayName tampering detected: incorrect code"
				break
			case 7:
			case 8:
				map.descriptionText = "$device.displayName detected motion"
				break
			default:
				map.descriptionText = "$device.displayName: security alarm $cmd.event"
				map.displayed = false
		}
	} else if (cmd.notificationType){
		map.descriptionText = "$device.displayName: alarm type $cmd.notificationType event $cmd.event"
	} else {
		map.descriptionText = "$device.displayName: alarm $cmd.v1AlarmType is ${cmd.v1AlarmLevel == 255 ? 'active' : cmd.v1AlarmLevel ?: 'inactive'}"
	}
	result ? [createEvent(map), *result] : createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "$device.displayName has a low battery"
        sendEvent(name: "lowBattery", value: "Replace Sensor Battery", descriptionText: map.descriptionText, displayed: true, isStateChange: true) // Register low Battery
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbatt = new Date().time
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
	result
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	updateDataValue("fw", fw)
	def text = "$device.displayName: firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
	createEvent(descriptionText: text, isStateChange: false)
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationBusy cmd) {
	def msg = cmd.status == 0 ? "try again later" :
	          cmd.status == 1 ? "try again in $cmd.waitTime seconds" :
	          cmd.status == 2 ? "request queued" : "sorry"
	createEvent(displayed: true, descriptionText: "$device.displayName is busy, $msg")
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	createEvent(displayed: true, descriptionText: "$device.displayName rejected the last request")
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(displayed: false, descriptionText: "$device.displayName: $cmd")
}

def open() {
	secure(zwave.barrierOperatorV1.barrierOperatorSet(requestedBarrierState: BarrierOperatorSet.REQUESTED_BARRIER_STATE_OPEN))
}

def close() {
	secure(zwave.barrierOperatorV1.barrierOperatorSet(requestedBarrierState: BarrierOperatorSet.REQUESTED_BARRIER_STATE_CLOSE))
}

def refresh() {
	log.trace "Refresh called"
    log.debug "Resetting low battery notifications"
	sendEvent(name: "lowBattery", value: "Sensor Battery OK", descriptionText: "$device.displayName door sensor has a OK battery", displayed: true, isStateChange: true) // Reset Battery Notification
	sendEvent(name: "battery", value: 100, unit: "%") // Reset battery level
    
	// Get the latest status
	delayBetween([
    	secure(zwave.barrierOperatorV1.barrierOperatorGet()),
    	zwave.batteryV1.batteryGet().format() // Try to get battery level
        ], 2000)
}

private secure(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private secureSequence(commands, delay=200) {
	delayBetween(commands.collect{ secure(it) }, delay)
}

def on() {
	open()
}

def off() {
	close()
}

def push() {    
    def latest = device.latestValue("door");
	log.debug "Garage door push button, current state $latest"

	switch (latest) {
    	case "open":
        	log.debug "Closing garage door"
        	close()
            break
            
        case "closed":
        	log.debug "Opening garage door"
        	open()
            break
            
        default:
        	log.debug "Can't change state of door, unknown state $latest"
            break
    }
}