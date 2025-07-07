<img align="left" src="https://github.com/nouranabdlsalam/patronus/blob/153bc75aa2d0452153e1cded952829ddbce8024a/app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.webp" alt="Patronus Logo" height="80">

# Patronus: AI-Powered Anti-Malware and Application-Level Network Threat Detection System for Android Devices

## Overview
Patronus is an Android security application developed as a graduation project at Alexandria University. It leverages AI to detect malware based on permission patterns and contextual behavior, while also monitoring network activity at the app level. The system is designed to run efficiently on physical Android devices without root access or elevated privileges.

## Project Thesis
🔗 [Click here](./Thesis.pdf) to read a comprehensive documentation of our methodology, malware profiling, neural network design, evaluation metrics, and system architecture.  
- **Chapter 1:** Introduction, problem statement, objectives, and contributions.
- **Chapter 2:** Background on Android security, malware types, and a comparative analysis of similar security applications.
- **Chapter 3:** Methodology, datasets, feature engineering, and AI model details.
- **Chapter 4:** System design, architecture, detailed component workflows, user interface designs, activity flow and use case diagrams.
- **Chapter 5:** Conclusion, insights, and future enhancements.
- **Appendix:** Key code snippets and technical implementations.
- **References:** Cited research papers, tools, and datasets.


## Team Members
| [<img src="https://github.com/nouranabdlsalam.png" width="70">](https://github.com/nouranabdlsalam) | [<img src="https://github.com/Amlkassem.png" width="70">](https://github.com/Amlkassem) | [<img src="https://github.com/ranwaasala.png" width="70">](https://github.com/ranwaasala) | [<img src="https://github.com/menna27ahmed.png" width="70">](https://github.com/menna27ahmed) | [<img src="https://github.com/Suhaila2Adel.png" width="70">](https://github.com/Suhaila2Adel) | [<img src="https://github.com/johnquriv.png" width="70">](https://github.com/johnquriv) |
|:---:|:---:|:---:|:---:|:---:|:---:|
| [Nouran Abdelsalam](https://github.com/nouranabdlsalam) | [Aml Kassem](https://github.com/Amlkassem) | [Ranwah Gamal](https://github.com/ranwaasala) | [Menna Allah Ahmed](https://github.com/menna27ahmed) | [Suhaila Adel](https://github.com/Suhaila2Adel) | [John George](https://github.com/johnquriv) |

## Key Features

Patronus is composed of seven core modules that work together to provide comprehensive mobile threat defense:

- **AI-Powered Malware Detection**: Uses permission-based neural networks enriched with behavioral profiles of six Android malware families.
- **Context-Aware Risk Scoring**: Dynamically adjusts threat evaluation based on app category and expected behavior to reduce false positives.
- **IP-Level Threat Monitoring**: Detects malicious app behavior by tracking outbound connections to known bad IPs.
- **Wi-Fi Threat Scanner**: Evaluates Wi-Fi network configurations for security issues and suggests mitigation.
- **Configurable Security Modes:** Three levels of protection (Low, Balanced, High) to suit user preferences for performance and security.
- **Threat Remediation**: Receives malware and network threats and proposes remediation options according to the threat type.
- **Interactive HelpBot**: A built-in assistant powered by an LLM that explains alerts and provides remediation tips.

 <br> <br>

### 🦠 AI-Powered Context-Aware Malware Detection System

<img align="right" src="demo/MalwareScanningDemo.gif" alt="Malware Scanner Demo" width="250">

- Detects malicious apps based on static permission behavior.
  <br> <br>
- Employs a neural network trained on three diverse malware datasets.
  <br> <br>
- Extracts and encodes permissions into binary feature vectors.
  <br> <br>
- Integrates six malware-type behavior profiles: Adware, Ransomware, Trojan, Spyware, Backdoor, Worm.
  <br> <br>
- Features a context-aware scoring layer to interpret app behavior relative to its category.
  <br> <br>
- Reduces false positives by balancing detection precision and functional expectations.
  <br> <br>
  <br> <br> <br> <br> <br> <br> 

  ---
  
### 🌐 App-Level IP Monitoring

<img align="right" src="demo/AppLevelNetThreatDetectionDemo.gif" alt="IP Monitor Demo" width="250">


- Periodically extracts and inspects outbound connections from each installed app.
  <br> <br> 
- Maintains a local database of known malicious IP addresses.
  <br> <br> 
- Flags suspicious IP traffic per app, highlighting the responsible process.
  <br> <br> 
- Uses a lightweight, permission-respecting foreground service (no root, no VPN).
  <br> <br> 
- Displays connection logs and raises alerts only when a known threat is identified.
  <br> <br> 
- Offers clear risk classification and actionable remediation suggestions.
  <br> <br>
  <br> <br> <br> <br> <br> <br>

  ---

### 🎛️ Configurable Security Modes

<img align="right" src="demo/SecModesandSysMonDemo.gif" alt="Security Modes Demo" width="250">

- Three predefined security levels: High Performance, High Security, and Balanced.
   <br> <br>
- **High Performance Mode**
  - No background monitoring for new app installs.
  - This mode is active when malware scanning is turned off.
   <br> <br>
- **High Security Mode**
  -  Activates background activity for monitoring new app installs.
  -  This mode is active when malware monitoring is turned on.
   <br> <br>
- **Balanced Mode**: Automatically switches between the previous two modes depending on the device state (battery levels, power saving status, etc.).
   <br> <br>
- Allows users to customize their security experience according to their needs and the required performance, creating a balance between device security and performance.
   <br> <br>
    <br> <br> <br> <br> <br> <br> 

---

### 🛜 Wi-Fi Threat Scanner

<img align="right" src="demo/WifiRiskAssessmentDemo.gif" alt="Wi-Fi Scanner Demo" width="250">

- Automatically evaluates the security of the current connected Wi-Fi network.
  <br> <br>
- Inspects encryption type, password protection, and general connection trust level.
  <br> <br>
- Flags insecure or open networks and scores them using internal risk metrics.
  <br> <br>
- Warns users when authentication is weak or missing altogether.
  <br> <br>
- Suggests steps to disconnect, switch networks, or enhance configuration.
  <br> <br>
- Runs automatically when the device connects to a new network.
  <br> <br>
  <br> <br> <br> <br> <br> <br> <br> <br>
  
---

### 🛜 Network Center

<img align="right" src="demo/NetCenterDemo.gif" alt="Network Center Demo" width="250">

- Provides three essential utilities for assessing network conditions:
  - **General Test**: Checks overall connection health and responsiveness.
  - **Speed Test**: Measures download and upload throughput to evaluate performance.
  - **Troubleshooting Module**: Identifies common misconfigurations or weak points in connectivity.
    <br> <br>
- Offers a clean interface with instant results and visual cues.
     <br> <br>   <br> <br>   <br> <br>   <br> <br>    <br> <br>   <br> <br>    <br> <br>

  --- 

### 💬 HelpBot (LLM-Powered Assistant)

<img align="right" src="demo/HelpbotDemo.gif" alt="HelpBot Demo" width="250">

- An AI assistant integrated inside the app using an API-based language model.
  <br> <br>
- Allows users to ask about malware scans, permission concerns, or network safety.
   <br> <br>
- Provides a more interactive experience than a standard FAQ section.
   <br> <br>
- Always accessible via the floating HelpBot icon in the app.
   <br> <br>
    <br> <br> <br> <br> <br> <br>  <br> <br> <br> <br>  <br> <br> <br> <br>

--- 

## Future Work
- **Android 10+ Compatibility:** Enhance network scanning capabilities to support Android 10+.
- **Voice Interaction for HelpBot:** Introduce hands-free voice commands to improve accessibility and user convenience.
- **Enhanced Database Integration:** Continuously expand the malicious IP database to improve proactive threat detection.
- **User Experience Improvements:** Refine the UI/UX based on user feedback to further streamline security interactions and system alerts.

## Acknowledgements
This project was completed under the supervision of Prof. Dr. Yasser Fouad, whose guidance and feedback were instrumental to both our research and implementation. We’re sincerely grateful for his time, support, and encouragement throughout the development of Patronus.
