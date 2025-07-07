<img align="left" src="https://github.com/nouranabdlsalam/patronus/blob/153bc75aa2d0452153e1cded952829ddbce8024a/app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.webp" alt="Patronus Logo" height="80">

# Patronus

Patronus is an AI-powered anti-malware and application-level network threat detection system for Android devices developed as a graduation project at Alexandria University. It leverages AI to detect malware based on permission patterns and contextual factors, while also monitoring network activity at the application level. The system is designed to run efficiently on physical Android devices without root access or elevated privileges.

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

<table>
<tr>
<td>
<ul>
  <li>Detects malicious apps based on static permission behavior.</li>
 <br><br>
  <li>Employs a neural network trained on three diverse malware datasets.</li>
 <br><br>
  <li>Extracts and encodes permissions into binary feature vectors.</li>
 <br><br>
  <li>Integrates six malware-type behavior profiles: Adware, Ransomware, Trojan, Spyware, Backdoor, Worm.</li>
 <br><br>
  <li>Features a context-aware scoring layer to reduce false positives.</li>
 <br><br>
  <li>Reduces false positives by balancing detection precision and functional expectations.</li>
 <br><br>
</ul>
</td>
<td >
  <img src="demo/MalwareScanningDemo.gif" alt="Malware Scanner Demo" width="250">
</td>
</tr>
</table>

  ---
  
### 🌐 App-Level IP Monitoring

<table>
<tr>
<td>

<ul>
  <li>Periodically extracts and inspects outbound connections from each installed app.</li>
 <br><br>
  <li>Maintains a local database of known malicious IP addresses.</li>
 <br><br>
  <li>Flags suspicious IP traffic per app, highlighting the responsible process.</li>
 <br><br>
  <li>Uses a lightweight, permission-respecting foreground service (no root, no VPN).</li>
 <br><br>
  <li>Offers clear risk classification and actionable remediation suggestions.</li>
 <br><br>
</ul>

</td>
<td>
  <img src="demo/AppLevelNetThreatDetectionDemo.gif" alt="IP Monitor Demo" width="250">
</td>
</tr>
</table>

  ---

### 🎛️ Configurable Security Modes

<table>
<tr>
<td>

<ul>
  <li><strong>Three predefined security levels:</strong> High Performance, High Security, and Balanced.</li>
 <br><br>
  <li><strong>High Performance Mode:</strong> No background monitoring for new app installs. Active when malware scanning is off.</li>
 <br><br>
  <li><strong>High Security Mode:</strong> Enables background activity to monitor new app installs. Active when malware monitoring is on.</li>
 <br><br>
  <li><strong>Balanced Mode:</strong> Auromatically switches between the previous two modes based on device state (battery level, power-saving mode, etc.).</li>
 <br><br>
  <li>Lets users balance security and performance based on individual preferences.</li>
 <br><br>
</ul>

</td>
<td>
  <img src="demo/SecModesandSysMonDemo.gif" alt="Security Modes Demo" width="250">
</td>
</tr>
</table>


---

### 🛜 Wi-Fi Threat Scanner

<table>
<tr>
<td>

<ul>
  <li>Automatically evaluates the security of the current connected Wi-Fi network. </li>
 <br><br>
  <li>Inspects encryption type, password protection, and general trust level.</li>
 <br><br>
  <li>Flags insecure or open networks using internal risk metrics.</li>
 <br><br>
  <li>Warns users when authentication is weak or missing.</li>
 <br><br>
  <li>Recommends disconnecting, switching networks, or improving settings.</li>
 <br><br>
</ul>

</td>
<td>
  <img src="demo/WifiRiskAssessmentDemo.gif" alt="Wi-Fi Scanner Demo" width="250">
</td>
</tr>
</table>

  
---

### 🛜 Network Center

<table>
<tr>
<td>

<ul>
  <li><strong>General Test:</strong> Evaluates basic connectivity and responsiveness.</li>
 <br><br>
  <li><strong>Speed Test:</strong> Measures upload/download performance.</li>
 <br><br>
  <li><strong>Troubleshooting Module:</strong> Identifies misconfigurations or network weaknesses.</li>
 <br><br>
  <li>Presents results clearly with visual cues.</li>
 <br><br>
</ul>

</td>
<td style="vertical-align: top; padding-right: 20px;">
  <img src="demo/NetCenterDemo.gif" alt="Network Center Demo" width="250">
</td>
</tr>
</table>


  --- 

### 💬 HelpBot (LLM-Powered Assistant)

<table>
<tr>
<td>

<ul>
  <li>AI assistant integrated via a language model API.</li>
 <br><br>
  <li>Answers user questions about malware scans, permissions, and network safety.</li>
 <br><br>
  <li>Provides a more interactive experience than traditional FAQ sections.</li>
 <br><br>
  <li>Always accessible through the floating HelpBot icon.</li>
 <br><br>
</ul>

</td>
<td style="vertical-align: top; padding-right: 20px;">
  <img src="demo/HelpbotDemo.gif" alt="HelpBot Demo" width="250">
</td>
</tr>
</table>


--- 

## Future Work
- **Android 10+ Compatibility:** Enhance network scanning capabilities to support Android 10+.
- **Voice Interaction for HelpBot:** Introduce hands-free voice commands to improve accessibility and user convenience.
- **Enhanced Database Integration:** Continuously expand the malicious IP database to improve proactive threat detection.
- **User Experience Improvements:** Refine the UI/UX based on user feedback to further streamline security interactions and system alerts.

## Acknowledgements
This project was completed under the supervision of Prof. Dr. Yasser Fouad, whose guidance and feedback were instrumental to both our research and implementation. We’re sincerely grateful for his time, support, and encouragement throughout the development of Patronus.
