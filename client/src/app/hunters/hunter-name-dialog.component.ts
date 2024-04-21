import { Component } from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Router } from "@angular/router";
import { HostService } from "../hosts/host.service";

@Component({
  selector: 'app-hunter-name-dialog',
  templateUrl: './hunter-name-dialog.component.html',
  styleUrls: ['./hunter-name-dialog.component.scss'],
  standalone: true,
})

export class HunterNameDialogComponent {
// Add MatDialog to the constructor
constructor(private hostService: HostService, private router: Router, private snackBar: MatSnackBar, private dialog: MatDialog) { }

// Add a new method to open the dialog
promptForHunterName() {
  const dialogRef = this.dialog.open(HunterNameDialogComponent);

  dialogRef.afterClosed().subscribe(result => {
    console.log(`Dialog result: ${result}`);
  });
}
}
