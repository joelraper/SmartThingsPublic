/**
 *  Copyright 2015 SmartThings
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
 *  Turn on and turn lights to White when...
 *
 *  Author: Joel Raper
 *
 *  Date: 2015-12-3
 */
definition(
    name: "A1 outside Light Control",
    namespace: "smartthings",
    author: "Joel Raper",
    description: "turn on Fibaro RGBW lights and change color when door opens, motion is detected, or someone arrives...then turns back to prescribed color",
    category: "My Apps",
    iconUrl: 		"https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet.png",
    iconX2Url: 		"https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet@2x.png"
)



preferences {
	section(""){
		 input "themotion", "capability.motionSensor", multiple: true, required: true, title: "Where?"
         	input "switches", "capability.switch", multiple: true, title: "Turn on this/these Fibaro Controller(s)..."
	        input(name: "switchAction", required: false, type: "enum", title: "And run this action (optional)", 
              options: ["red", "green", "blue", "white", "cyan", "magenta", "orange", "purple", "yellow",
        	            "pink", "coldWhite", "warmWhite", "fireplace", "storm", "deepfade", "litefade", "police"])
         input(name: "switchAction2", required: false, type: "enum", title: "And run this action when motion is done(optional)", 
              options: ["red", "green", "blue", "white", "cyan", "magenta", "orange", "purple", "yellow",
        	            "pink", "coldWhite", "warmWhite", "fireplace", "storm", "deepfade", "litefade", "police"])
    }
  }

def installed() {
	log.info "installed with $settings"
	subscribe(themotion, "motion.active", motionDetectedHandler)
    subscribe(themotion, "motion.inactive", motionStoppedHandler)
	initialize()
}

def updated() {
	log.info "updated with $settings"
	unsubscribe()
	subscribe(themotion, "motion.active", motionDetectedHandler)
    subscribe(themotion, "motion.inactive", motionStoppedHandler)
    initialize()
}

def initialize() {
	state.actionList = ["red", "green", "blue", "white", "cyan", "magenta", "orange", "purple", "yellow",
        	            "pink", "coldWhite", "warmWhite", "fireplace", "storm", "deepfade", "litefade", "police"]
    state.actionList2 = ["red", "green", "blue", "white", "cyan", "magenta", "orange", "purple", "yellow",
        	            "pink", "coldWhite", "warmWhite", "fireplace", "storm", "deepfade", "litefade", "police"]
}

def motionDetectedHandler(evt) {
	if ( switchAction ) {
    	switches."${switchAction}"()
    } else {
    	def cnt = counterHandler()    
		log.trace "Turning on Controllers $switches with: ${state.actionList.get(cnt)}"
		switches."${state.actionList.get(cnt)}"()
    }
}
def motionStoppedHandler(evt) 
	 {
       def cnt = counterHandler()    
		log.trace "Turning on Controllers $switches with: ${"warmwhite"}"
		switches."${"green"}"()

}


def counterHandler() {
    if ( (state.actionCounter == null) || (state.actionCounter >= (state.actionList.size() - 1)) )
    	state.actionCounter = 0
    else
    	state.actionCounter = state.actionCounter + 1   
    return state.actionCounter
}





/*


        input "themotion", "capability.motionSensor", required: true, title: "Where?"
    }
        subscribe(themotion, "motion.active", motionDetectedHandler)
        
        */