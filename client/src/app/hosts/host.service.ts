import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, map, tap, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { Hunt } from '../hunts/hunt';
import { Task } from '../hunts/task';
import { CompleteHunt } from '../hunts/completeHunt';
import { StartedHunt } from '../startHunt/startedHunt';
//import { EndedHunt } from '../endedHunts/endedHunt';
import { Team } from '../hunters/join-hunt/team';
import { Submission } from '../hunters/join-hunt/Submission';
//import { Team } from '../hunters/join-hunt/team';

@Injectable({
  providedIn: 'root'
})
export class HostService {
  readonly hostUrl: string = `${environment.apiUrl}hosts`;
  readonly huntUrl: string = `${environment.apiUrl}hunts`;
  readonly taskUrl: string = `${environment.apiUrl}tasks`;
  readonly startHuntUrl: string = `${environment.apiUrl}startHunt`;
  readonly startedHuntUrl: string = `${environment.apiUrl}startedHunts`;
  readonly endHuntUrl: string = `${environment.apiUrl}endHunt`;
  readonly endedHuntsUrl: string = `${environment.apiUrl}endedHunts`;
  readonly endedHuntUrl: string = `${environment.apiUrl}startedHunt`;
  //readonly addTeamsUrl: string = `${environment.apiUrl}addTeams`;

  constructor(private httpClient: HttpClient){
  }

  getHunts(hostId: string): Observable<Hunt[]> {
    return this.httpClient.get<Hunt[]>(`${this.hostUrl}/${hostId}`);
  }

  getHuntById(id: string): Observable<CompleteHunt> {
    return this.httpClient.get<CompleteHunt>(`${this.huntUrl}/${id}`);
  }

  addHunt(newHunt: Partial<Hunt>): Observable<string> {
    newHunt.hostId = "588945f57546a2daea44de7c";
    return this.httpClient.post<{id: string}>(this.huntUrl, newHunt).pipe(map(result => result.id));
  }

  addTask(newTask: Partial<Task>): Observable<string> {
    return this.httpClient.post<{id: string}>(this.taskUrl, newTask).pipe(map(res => res.id));
  }

  deleteHunt(id: string): Observable<void> {
    return this.httpClient.delete<void>(`/api/hunts/${id}`);
  }

  deleteTask(id: string): Observable<void> {
    return this.httpClient.delete<void>(`/api/tasks/${id}`);
  }

  startHunt(id: string): Observable<string> {
    return this.httpClient.get<string>(`${this.startHuntUrl}/${id}`);
  }

  getStartedHunt(accessCode: string): Observable<StartedHunt> {
    return this.httpClient.get<StartedHunt>(`${this.startedHuntUrl}/${accessCode}`);
  }

  // This is a put request that ends the hunt by setting its status to false
  endStartedHunt(id: string): Observable<void> {
    return this.httpClient.put<void>(`${this.endHuntUrl}/${id}`, null);
  }

  // This is a get request that gets all the ended StartedHunts
  getEndedHunts(hostId: string): Observable<StartedHunt[]> {
    return this.httpClient.get<StartedHunt[]>(`${this.hostUrl}/${hostId}/endedHunts`);
  }

  // This is a delete request that deletes the ended StartedHunt
  deleteEndedHunt(id: string): Observable<void> {
    return this.httpClient.delete<void>(`${this.endedHuntsUrl}/${id}`);
  }

  submitPhoto(startedHuntId: string, taskId: string, photo: File, teamId: string): Observable<string> {
    const formData = new FormData();
    formData.append('photo', photo);
    return this.httpClient.post<{id: string}>(`${this.endedHuntUrl}/${startedHuntId}/tasks/${taskId}/photo/${teamId}`, formData).pipe(map(result => result.id));
  }

  replacePhoto(startedHuntId: string, taskId: string, photoPath: string, photo: File, teamId: string): Observable<string> {
    const formData = new FormData();
    formData.append('photo', photo);
    return this.httpClient.put<{id: string}>(`${this.endedHuntUrl}/${startedHuntId}/tasks/${taskId}/photo/${photoPath}/${teamId}`, formData).pipe(map(result => result.id));
  }

  getEndedHuntById(id: string): Observable<StartedHunt> {
    return this.httpClient.get<StartedHunt>(`${this.endedHuntsUrl}/${id}`);
  }

// This takes in a integer from the host and adds that amount of empty teams to the startedHunt
addTeams(startedHuntId: string, numTeams: number): Observable<void> {
  return this.httpClient.post<void>(`/api/teams/create?startedHuntId=${startedHuntId}&numTeams=${numTeams}`,null);
}

getTeams(startedHuntId: string): Observable<Team[]> {
  return this.httpClient.get<Team[]>(`/api/startedHunts/${startedHuntId}/teams`);
}

getTeamSubmissions(teamId: string): Observable<Submission[]> {
  return this.httpClient.get<Submission[]>(`/api/submissions/team/${teamId}`);
}

getPhoto(submissionId: string): Observable<string> {
  console.log(`Client: Sending request to get photo for submissionId: ${submissionId}`);
  return this.httpClient.get(`/api/submissions/${submissionId}/photo`, { responseType: 'text' })
    .pipe(
      tap(() => console.log(`Client: Received photo for submissionId: ${submissionId}`)),
      catchError(error => {
        console.error(`Client: Error getting photo for submissionId: ${submissionId}`, error);
        return throwError(() => error);
      })
    );
}

convertToImageSrc(base64: string): string {
  if (base64.startsWith('data:image/png;base64,')) {
    return base64;
  }
  return 'data:image/base64,' + base64;
}

getSubmissionsByStartedHunt(startedHuntId: string): Observable<Submission[]> {
  return this.httpClient.get<Submission[]>(`/api/submissions/startedHunt/${startedHuntId}`);
}

getAllStartedHuntTeams(startedHuntId: string): Observable<Team[]> {
  return this.httpClient.get<Team[]>(`/api/startedHunts/${startedHuntId}/teams`);
}

getStartedHuntById(id: string): Observable<StartedHunt> {
  return this.httpClient.get<StartedHunt>(`/api/startedHunt/${id}`);
}
}
