import { Component, inject, signal, computed } from "@angular/core";
import { bootstrapApplication } from "@angular/platform-browser";
import {
  provideHttpClient,
  HttpClient,
  HttpErrorResponse,
} from "@angular/common/http";
import { FormsModule } from "@angular/forms";
interface Customer {
  customer_id: number;
  name: string;
  address_line1: string;
  address_line2: string;
  city: string;
  state: string;
  zip: string;
}
type Draft = Omit<Customer, "customer_id">;
const blank = (): Draft => ({
  name: "",
  address_line1: "",
  address_line2: "",
  city: "",
  state: "",
  zip: "",
});
@Component({
  selector: "app-root",
  standalone: true,
  imports: [FormsModule],
  templateUrl: "./app.html",
})
class App {
  private http = inject(HttpClient);
  customers = signal<Customer[]>([]);
  query = signal("");
  loading = signal(true);
  busy = signal(false);
  message = signal("");
  error = signal("");
  editing = signal<number | null>(null);
  pendingDelete = signal<Customer | null>(null);
  draft: Draft = blank();
  filtered = computed(() => {
    const q = this.query().toLowerCase();
    return this.customers().filter((c) =>
      [c.name, c.city, c.state, c.zip, String(c.customer_id)]
        .join(" ")
        .toLowerCase()
        .includes(q),
    );
  });
  constructor() {
    this.load();
  }
  load() {
    this.loading.set(true);
    this.http.get<Customer[]>("/api/customers").subscribe({
      next: (rows) => {
        this.customers.set(rows);
        this.loading.set(false);
      },
      error: (e) => {
        this.fail(e);
        this.loading.set(false);
      },
    });
  }
  fail(e: HttpErrorResponse) {
    const d = e.error?.detail;
    this.error.set(
      typeof d === "string"
        ? d
        : Array.isArray(d)
          ? d.map((x: any) => x.loc.at(-1) + ": " + x.msg).join("; ")
          : "Unable to connect. Check that the API and database are running.",
    );
    this.busy.set(false);
  }
  edit(c: Customer) {
    const { customer_id, ...rest } = c;
    this.editing.set(customer_id);
    this.draft = { ...rest };
    this.message.set("");
    this.error.set("");
  }
  cancel() {
    this.editing.set(null);
    this.draft = blank();
  }
  save() {
    this.busy.set(true);
    this.error.set("");
    this.message.set("");
    const id = this.editing();
    const body = { ...this.draft, state: this.draft.state.toUpperCase() };
    const req =
      id === null
        ? this.http.post<Customer>("/api/customers", body)
        : this.http.put<Customer>("/api/customers/" + id, body);
    req.subscribe({
      next: () => {
        this.busy.set(false);
        this.message.set(
          id === null
            ? "Customer created successfully."
            : "Customer updated successfully.",
        );
        this.cancel();
        this.load();
      },
      error: (e) => this.fail(e),
    });
  }
  remove() {
    const c = this.pendingDelete();
    if (!c) return;
    this.busy.set(true);
    this.error.set("");
    this.http.delete("/api/customers/" + c.customer_id).subscribe({
      next: () => {
        this.busy.set(false);
        this.pendingDelete.set(null);
        if (this.editing() === c.customer_id) this.cancel();
        this.message.set("Customer deleted successfully.");
        this.load();
      },
      error: (e) => {
        this.pendingDelete.set(null);
        this.fail(e);
      },
    });
  }
}
bootstrapApplication(App, { providers: [provideHttpClient()] }).catch(
  console.error,
);
