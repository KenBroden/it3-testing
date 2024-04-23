import { CompleteHunt } from "../hunts/completeHunt";

export interface StartedHunt {
  _id: string;
  completeHunt: CompleteHunt;
  accessCode: string;
  status: boolean;
  endDate?: Date;
  submissionIds?: string[];

}
