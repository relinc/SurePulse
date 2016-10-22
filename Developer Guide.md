#SURE-Pulse Software Suite Developer Guide
Sure-Pulse is written in Java using the latest Java UI Library, JavaFX. Developers of Sure-Pulse use the Eclipse IDE. Many of the GUIs in SurePulse are in fxml format, which were created in SceneBuilder.
##Overview
![Flowchart](https://github.com/relinc/SurePulseDataProcessor/blob/master/SUREPulseSoftwareFlowChart.png)
###The 3 most important repositories are SPLibraries, SurePulseDataProcessor, and SurePulseViewer.
1. SPLibraries

   Contains common classes that are used by SurePulseDataProcessor and SurePulseViewer
2. SurePulseDataProcessor

   Pictured in red above, the Processor is responsible for creating bar setups and samples.
3. SurePulseViewer

   Pictured in blue above, the Viewer can load saved samples to display graphs and export to Excel.
