import { Component, OnInit } from '@angular/core';
import { Team } from '../team';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-select-team',
  standalone: true,
  imports: [],
  templateUrl: './select-team.component.html',
  styleUrl: './select-team.component.scss'
})
export class SelectTeamComponent implements OnInit {
  teams: Team[];
  startedHuntId: string;

  constructor(private route: ActivatedRoute, private http: HttpClient, private router: Router) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.startedHuntId = params['startedHuntId'];
      this.http.get<Team[]>(`/api/startedHunts/${this.startedHuntId}/teams`).subscribe(teams => {
        this.teams = teams;
      });
    });
  }

  selectTeam(teamId: string): void {
    // Navigate to the hunter view page with the selected team
    this.router.navigate(['/hunter-view', this.startedHuntId, teamId]);
  }

}
