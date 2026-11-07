package controller

import javafx.fxml.{FXML, Initializable}
import java.net.URL
import java.util.ResourceBundle

class AboutController extends Initializable {
  var onClose: () => Unit = () => ()
  override def initialize(url: URL, rb: ResourceBundle): Unit = {}
  @FXML
   def handleAClose(): Unit = onClose()
}

