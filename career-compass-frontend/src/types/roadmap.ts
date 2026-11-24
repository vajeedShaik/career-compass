export interface ResourceUsed {
  skill: string;
  title: string;
  url: string;
}

export interface RoadmapResponse {
  id: string;
  matchId: string;
  roadmapContent: string;
  resourcesUsed: ResourceUsed[];
  emailSent: boolean;
  createdAt: string;
}

export interface ChatMessageDto {
  role: "user" | "assistant";
  content: string;
  createdAt: string;
}

export interface ChatResponse {
  roadmapId: string;
  messages: ChatMessageDto[];
}
