/**
 *  Alexa Helper-Parent
 *
 *  Copyright 2015 Michael Struck
 *  Version 3.3.4 12/26/15
 * 
 *  Version 1.0.0 - Initial release
 *  Version 2.0.0 - Added 6 slots to allow for one app to control multiple on/off actions
 *  Version 2.0.1 - Changed syntax to reflect SmartThings Routines (instead of Hello, Home Phrases)
 *  Version 2.1.0 - Added timers to the first 4 slots to allow for delayed triggering of routines or modes
 *  Version 2.2.1 - Allow for on/off control of switches and changed the UI slightly to allow for other controls in the future
 *  Version 2.2.2 - Fixed an issue with slot 4
 *  Version 3.0.0 - Allow for parent/child 'slots'
 *  Version 3.1.0 - Added ability to control a thermostat
 *  Version 3.1.1 - Refined thermostat controls and GUI (thanks to @SDBOBRESCU "Bobby")
 *  Version 3.2.0 - Added ability to a connected speaker
 *  Version 3.3.0 - Added ability to change modes on a thermostat
 *  Version 3.3.1 - Fixed a small GUI misspelling
 *  Version 3.3.2 - Added option for triggering URLs when Alexa switch trips
 *  Version 3.3.3 - Added version number for child apps within main parent app
 *  Version 3.3.4 - Updated instructions, moved the remove button, fixed code variables and GUI options
 * 
 *  Uses code from Lighting Director by Tim Slagle & Michael Struck
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

definition(
    name: "Alexa Helper",
    singleInstance: true,
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Allows for various SmartThings functions to be tied to switches controlled by Amazon Echo('Alexa').",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/Alexa.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/Alexa@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/Alexa@2x.png")

preferences {
    page name:"mainPage"
    page name:"pageAbout"
}

//Show main page
def mainPage() {
	dynamicPage(name: "mainPage", title: "Alexa Helper Scenarios", install: true, uninstall: false) {
		section {
			app(name: "childScenarios", appName: "Alexa Helper-Scenario", namespace: "MichaelStruck", title: "Create New Alexa Scenario...", multiple: true)
		}
		section ("Thermostat") {
			href "tstatControl", title: "Thermostat Controls", description: getDescTstat(), state: greyOut(vDimmerTstat, tstat)
		}
		section ("Speaker") {
			href "speakerControl", title: "Speaker Controls", description: getDescSpeaker(), state: greyOut(vDimmerSpeaker, speaker)
		}
		section([title:"Options", mobileOnly:true]) {
			label title:"Assign a name", required:false
			href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license, instructions or to remove the application"
		}
	}
}

page(name: "tstatControl", title: "Thermostat Controls"){
   	section {
    	input "vDimmerTstat", "capability.switchLevel", title: "Alexa Dimmer Switch", multiple: false, required:false
		input "tstat", "capability.thermostat", title: "Thermostat To Control", multiple: false , required: false
	}
    section ("Thermostat Temperature Settings") {
        input "upLimitTstat", "number", title: "Thermostat Upper Limit", required: false
    	input "lowLimitTstat", "number", title: "Thermostat Lower Limit", required: false
        input "autoControlTstat", "bool", title: "Control when thermostat in 'Auto' mode", defaultValue: false
     }
     section ("Thermostat Mode Settings") {
        input "heatingSwitch", "capability.switch", title: "Heating Mode Switch", multiple: false, required: false
        input "coolingSwitch", "capability.switch", title: "Cooling Mode Switch", multiple: false, required: false
        input "autoSwitch", "capability.switch", title: "Auto Mode Switch", multiple: false, required: false
        input "heatingSetpoint", "number", title: "Heating setpoint", required: false
        input "coolingSetpoint", "number", title: "Cooling setpoint", required: false
	}
}

page (name: "speakerControl", title: "Speaker Controls"){
   	section {
        input "vDimmerSpeaker", "capability.switchLevel", title: "Alexa Dimmer Switch", multiple: false, required:false
		input "speaker", "capability.musicPlayer", title: "Connected Speaker To Control", multiple: false , required: false
	}
    section ("Speaker Volume Controls") {        
        input "speakerInitial", "number", title: "Volume when speaker turned on", required: false
        input "upLimitSpeaker", "number", title: "Volume Upper Limit", required: false
    	input "lowLimitSpeaker", "number", title: "Volume  Lower Limit", required: false
	}
	section ("Speaker Track Controls") {    
        input "nextSwitch", "capability.switch", title: "Next Track Switch", multiple: false, required: false
       	input "prevSwitch", "capability.switch", title: "Previous Track Switch", multiple: false, required: false
    }
}

def pageAbout(){
	dynamicPage(name: "pageAbout", title: "About ${textAppName()}", uninstall: true) {
		section {
    		paragraph "${textVersion()}\n${textCopyright()}\n\n${textLicense()}\n"
    	}
    	section("Instructions") {
        	paragraph textHelp()
    	}
        section("Tap button below to remove all scenarios and application"){
        }
	}
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    childApps.each {child ->
		log.info "Installed Scenario: ${child.label}"
    }
	if (vDimmerTstat && tstat) {
    	subscribe (vDimmerTstat, "level", "thermoHandler")
        if (heatingSwitch) {
        	subscribe (heatingSwitch, "switch", "heatHandler")
        }
        if (coolingSwitch) {
        	subscribe (coolingSwitch, "switch", "coolHandler")
        }
        if (autoSwitch) {
        	subscribe (autoSwitch, "switch", "autoHandler")
        }
	}
    if (vDimmerSpeaker && speaker) {
    	subscribe (vDimmerSpeaker, "level", "speakerVolHandler")
        subscribe (vDimmerSpeaker, "switch", "speakerOnHandler")
        if (nextSwitch) {
        	subscribe (nextSwitch, "switch", "controlNextHandler")
        }
        if (prevSwitch) {
        	subscribe (prevSwitch, "switch", "controlPrevHandler")
        } 
	}
}

//Thermostat Handler
def thermoHandler(evt){
    // Get settings between limits
    def tstatLevel = vDimmerTstat.currentValue("level") as int
    if (upLimitTstat && vDimmerTstat.currentValue("level") > upLimitTstat){
    	tstatLevel = upLimitTstat
    }
    if (lowLimitTstat && vDimmerTstat.currentValue("level") < lowLimitTstat){
    	tstatLevel = lowLimitTstat
    }
	//Turn thermostat to proper level depending on mode
    def tstatMode=tstat.currentValue("thermostatMode")
    if (tstatMode == "heat") {
        tstat.setHeatingSetpoint(tstatLevel)	
    }
    if (tstatMode == "cool") {
        tstat.setCoolingSetpoint(tstatLevel)	
    }
    if (tstatMode == "auto" && autoControlTstat){
    	tstat.setHeatingSetpoint(tstatLevel)
        tstat.setCoolingSetpoint(tstatLevel)
    }
    log.debug "Thermostat set to ${tstatLevel}"
}

//Speaker Volume Handler
def speakerVolHandler(evt){
    def speakerLevel = vDimmerSpeaker.currentValue("level") as int
    if (speakerLevel == 0) {
    	vDimmerSpeaker.off()	
    }
    else {
        // Get settings between limits
        if (upLimitSpeaker && vDimmerSpeaker.currentValue("level") > upLimitSpeaker){
    		speakerLevel = upLimitSpeaker
    	}
    	if (lowLimitSpeaker && vDimmerSpeaker.currentValue("level") < lowLimitSpeaker){
    		speakerLevel = lowLimitSpeaker
    	}
		//Turn speaker to proper volume
    	speaker.setLevel(speakerLevel)
	}
}

//Speaker on/off
def speakerOnHandler(evt){
	if (evt.value == "on"){
    	if (speakerInitial){
        	def speakerLevel = speakerInitial as int
    		vDimmerSpeaker.setLevel (speakerInitial)
        }
    	speaker.play()
    }
    else {
    	speaker.stop()
    }
}

def controlNextHandler(evt){
    speaker.nextTrack()
}

def controlPrevHandler(evt){
	speaker.previousTrack()
}

//Thermostat mode change
def heatHandler(evt){
	tstat.heat()
    if (heatingSetpoint){
    	tstat.setHeatingSetpoint(heatingSetpoint)
    }
}

def coolHandler(evt){
	tstat.cool()
    if (coolingSetpoint){
    	tstat.setCoolingSetpoint(coolingSetpoint)
    }
}

def autoHandler(evt){
	tstat.auto()
    if (heatingSetpoint){
        tstat.setHeatingSetpoint(heatingSetpoint)
    }
    if (coolingSetpoint){
    	tstat.setCoolingSetpoint(coolingSetpoint)
    }
}

//Common Methods

def getDescTstat(){
    def result = "Tap to setup thermostat controls"
    if (vDimmerTstat && tstat) {
		result = "${vDimmerTstat} controls ${tstat}"
        result += upLimitTstat ? "\nLimits: No greater than ${upLimitTstat}" : ""
        result += upLimitTstat && lowLimitTstat ? " and no lower than ${lowLimitTstat}" : ""
        result += !upLimitTstat && lowLimitTstat ? "\nLimits: No lower than ${lowLimitTstat}" : ""
        result += autoControlTstat ? "\nControl when thermostat in 'Auto' mode" : ""
		result += heatingSwitch ? "\nHeating Switch: ${heatingSwitch}" : ""
        result += heatingSwitch && heatingSetpoint ? " set to ${heatingSetpoint}" : ""
        result += coolingSwitch ? "\nCooling Switch: ${coolingSwitch}" : ""
        result += coolingSwitch && coolingSetpoint ? " set to ${coolingSetpoint}" : ""
        result += autoSwitch ? "\nAuto Switch: ${autoSwitch}" : ""
		result += autoSwitch && heatingSetpoint ? " Heat Setting: ${heatingSetpoint}" : ""
        result += autoSwitch && coolingSetpoint ? " Cool Setting: ${coolingSetpoint}" : ""
	}
    result
}

def getDescSpeaker(){
    def result = "Tap to setup speaker controls"
    if (vDimmerSpeaker && speaker) {
		result = "${vDimmerSpeaker} controls ${speaker}"
        result += upLimitSpeaker ? "\nVolume Limits: No greater than ${upLimitSpeaker}" : ""
        result += lowLimitSpeaker && upLimitSpeaker ? " and no lower than ${lowLimitSpeaker}" : ""
        result += lowLimitSpeaker && !upLimitSpeaker ? "\nVolume Limits: No lower than ${lowLimitSpeaker}" : ""
        result += nextSwitch ? "\nNext Track Switch: ${nextSwitch}" : ""
        result += prevSwitch ? "\nPrevious Track Switch: ${prevSwitch}" : ""
	}
    result
}

def greyOut(param1,param2){
    def result = param1 && param2 ? "complete" : ""
}

//Version/Copyright/Information/Help

private def textAppName() {
	def text = "Alexa Helper"
}	

private def textVersion() {
    def version = "Parent App Version: 3.3.4 (12/26/2015)"
    def childCount = childApps.size()
    def childVersion = "No scenarios installed"
    if (childCount){
    	childApps.each {child ->
        	childVersion=child.textVersion()       
    	}
	} 
    return "${version}\n${childVersion}"
}

private def textCopyright() {
    def text = "Copyright © 2015 Michael Struck"
}

private def textLicense() {
    def text =
		"Licensed under the Apache License, Version 2.0 (the 'License'); "+
		"you may not use this file except in compliance with the License. "+
		"You may obtain a copy of the License at"+
		"\n\n"+
		"    http://www.apache.org/licenses/LICENSE-2.0"+
		"\n\n"+
		"Unless required by applicable law or agreed to in writing, software "+
		"distributed under the License is distributed on an 'AS IS' BASIS, "+
		"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. "+
		"See the License for the specific language governing permissions and "+
		"limitations under the License."
}

private def textHelp() {
	def text =
		"Ties various SmartThings functions to the on/off state of a specifc switch. You may also control a thermostat or the volume of a wireless speaker using a dimmer control. "+
		"Perfect for use with the Amazon Echo ('Alexa').\n\nTo use, first create the required momentary button tiles or virtual switches/dimmers from the SmartThings IDE. "+
		"You may also use any physical switches already associated with SmartThings. Include these switches within the Echo/SmartThings app, then discover the switches on the Echo. "+
		"For on/off or momentary buttons, add a scenario and choose the discovered switch to be monitored and tie the on/off state of that switch to a specific routine, mode, URL, Smart Home Monitor "+
        	"security state or the on/off state of other switches. The chosen functions or switches will fire when the main switch changes, except in cases where you have a delay specified. "+ 
        	"This time delay is optional. "+
		"\n\nPlease note that if you are using a momentary switch you should only define the 'on' action within each scenario.\n\n" +
		"To control a thermostat, tap the thermostat controls and choose a dimmer switch (usually a virtual dimmer) and the thermostat you wish to control. "+
		"You can also limit the range the thermostat will reach (for example, even if you accidently set the dimmer to 100, the value sent to the "+
		"thermostat could be limited to 72). You can also add momentary switches to activate the thermostat from heating, cooling, or auto modes."+
		"\nTo control a connected speaker, tap the speaker controls and choose a dimmer switch (usually a virtual dimmer) and speaker you wish to control. "+
		"You can set the initial volume upon turning on the speaker, along with volume limites. Finally. you can utilize other virtual switches to choose next/previous tracks."
}