
namespace De.Osthus.Minerva.Busy
{
    public class BusyToken : IBusyToken
    {
        protected readonly BusyController busyController;

        public BusyToken(BusyController busyController)
        {
            this.busyController = busyController;
        }

        public void Finished()
        {
            busyController.Finished(this);
        }
    }
}
