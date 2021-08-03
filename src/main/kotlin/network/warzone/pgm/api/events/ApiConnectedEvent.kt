package network.warzone.pgm.api.events

import network.warzone.pgm.api.ApiClient
import network.warzone.pgm.utils.KEvent

class ApiConnectedEvent(val apiClient: ApiClient) : KEvent()