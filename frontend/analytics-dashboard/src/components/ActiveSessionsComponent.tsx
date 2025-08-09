import {
  Card, CardContent, CardDescription, CardHeader, CardTitle,
} from "@/components/ui/card";
import {
  Table, TableBody, TableCaption, TableCell,
  TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import { type UserDetails } from "@/model/UserDetails";

type ActiveSessionsProps = {
  data: UserDetails[];
  loading?: boolean;
  className?: string;
};

const ActiveSessionsComponent = ({ data, loading = false, className }: ActiveSessionsProps) => {
  return (
    <Card className={`w-full ${className ?? ""}`}>
      <CardHeader>
        <CardTitle>User Sessions</CardTitle>
        <CardDescription>Active sessions in the last five minutes</CardDescription>
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="space-y-2">
            <Skeleton className="h-5 w-40" />
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-3/4" />
          </div>
        ) : (
          <Table>
            <TableCaption>Active sessions in last five minutes.</TableCaption>
            <TableHeader>
              <TableRow>
                <TableHead className="w-[160px]">User ID</TableHead>
                <TableHead className="text-right">Session Count</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={2} className="text-center text-muted-foreground">
                    No active sessions.
                  </TableCell>
                </TableRow>
              ) : (
                data.map(({ userId, sessionCount }) => (
                  <TableRow key={userId}>
                    <TableCell className="font-medium">{userId}</TableCell>
                    <TableCell className="text-right">{sessionCount}</TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  );
};

export default ActiveSessionsComponent;
