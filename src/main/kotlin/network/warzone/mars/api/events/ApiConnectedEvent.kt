package network.warzone.mars.api.events

import network.warzone.mars.api.ApiClient
import network.warzone.mars.utils.KEvent

class ApiConnectedEvent(val apiClient: ApiClient) : KEvent()